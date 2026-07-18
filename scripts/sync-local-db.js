// Sincroniza las 4 bases de datos locales (Docker Compose) con los datos
// reales que viven en Railway, para que el entorno local no arranque vacio.
//
// Uso:
//   1. cd scripts && npm install
//   2. copiar .env.example a .env y pedirle las credenciales reales de Railway
//      a quien administre el proyecto (no estan en el repo por seguridad)
//   3. node sync-local-db.js
//
// Requiere que docker compose ya este arriba y sano (docker compose ps).

require('dotenv').config();
const { Client } = require('pg');
const mysql = require('mysql2/promise');
const sql = require('mssql');

function required(name) {
  const value = process.env[name];
  if (!value) {
    console.error(`Falta la variable de entorno ${name}. Revisa tu archivo .env (basado en .env.example).`);
    process.exit(1);
  }
  return value;
}

async function syncPostgres(label, srcConn, dstConn, tables) {
  const src = new Client({ connectionString: srcConn });
  const dst = new Client({ connectionString: dstConn });
  await src.connect();
  await dst.connect();

  await dst.query(`TRUNCATE ${tables.join(',')} RESTART IDENTITY CASCADE`);

  for (const t of tables) {
    const rows = (await src.query(`SELECT * FROM ${t}`)).rows;
    if (rows.length === 0) { console.log(`${label}.${t}: 0 filas`); continue; }
    const cols = Object.keys(rows[0]);
    for (const row of rows) {
      const placeholders = cols.map((_, i) => `$${i + 1}`).join(',');
      const values = cols.map((c) => row[c]);
      await dst.query(`INSERT INTO ${t} (${cols.join(',')}) OVERRIDING SYSTEM VALUE VALUES (${placeholders})`, values);
    }
    const pkCol = await dst.query(
      `SELECT a.attname FROM pg_index i
       JOIN pg_attribute a ON a.attrelid = i.indrelid AND a.attnum = ANY(i.indkey)
       WHERE i.indrelid = $1::regclass AND i.indisprimary`,
      [t]
    );
    if (pkCol.rows[0]) {
      const col = pkCol.rows[0].attname;
      await dst.query(
        `SELECT setval(pg_get_serial_sequence('${t}', '${col}'), COALESCE((SELECT MAX(${col}) FROM ${t}), 1))`
      ).catch(() => null);
    }
    console.log(`${label}.${t}: ${rows.length} filas sincronizadas`);
  }
  await src.end();
  await dst.end();
}

async function syncMysql(label, srcCfg, dstCfg, tables) {
  const src = await mysql.createConnection(srcCfg);
  const dst = await mysql.createConnection(dstCfg);
  await dst.query('SET FOREIGN_KEY_CHECKS=0');
  for (const t of tables) {
    await dst.query(`TRUNCATE TABLE ${t}`);
  }
  for (const t of tables) {
    const [rows] = await src.execute(`SELECT * FROM ${t}`);
    if (rows.length === 0) { console.log(`${label}.${t}: 0 filas`); continue; }
    const cols = Object.keys(rows[0]);
    for (const row of rows) {
      const placeholders = cols.map(() => '?').join(',');
      const values = cols.map((c) => row[c]);
      await dst.execute(`INSERT INTO ${t} (${cols.join(',')}) VALUES (${placeholders})`, values);
    }
    console.log(`${label}.${t}: ${rows.length} filas sincronizadas`);
  }
  await dst.query('SET FOREIGN_KEY_CHECKS=1');
  await src.end();
  await dst.end();
}

async function syncMssql(label, srcCfg, dstCfg, tables) {
  const srcPool = new sql.ConnectionPool(srcCfg);
  await srcPool.connect();
  const dstPool = new sql.ConnectionPool({ ...dstCfg, pool: { max: 1, min: 1 } });
  await dstPool.connect();

  await dstPool.request().batch('ALTER TABLE facturas NOCHECK CONSTRAINT ALL');
  for (const t of [...tables].reverse()) {
    await dstPool.request().batch(`DELETE FROM ${t}`);
    await dstPool.request().batch(`DBCC CHECKIDENT ('${t}', RESEED, 0)`);
  }
  await dstPool.request().batch('ALTER TABLE facturas WITH CHECK CHECK CONSTRAINT ALL');

  for (const t of tables) {
    const r = await srcPool.request().query(`SELECT * FROM ${t}`);
    const rows = r.recordset;
    if (rows.length === 0) { console.log(`${label}.${t}: 0 filas`); continue; }
    const cols = Object.keys(rows[0]);

    await dstPool.request().batch(`SET IDENTITY_INSERT ${t} ON`);
    for (const row of rows) {
      const req = dstPool.request();
      cols.forEach((c) => req.input(c, row[c]));
      const colList = cols.join(',');
      const paramList = cols.map((c) => `@${c}`).join(',');
      await req.query(`INSERT INTO ${t} (${colList}) VALUES (${paramList})`);
    }
    await dstPool.request().batch(`SET IDENTITY_INSERT ${t} OFF`);
    console.log(`${label}.${t}: ${rows.length} filas sincronizadas`);
  }
  await dstPool.close();
  await srcPool.close();
}

async function main() {
  console.log('=== AUTH (Postgres) ===');
  await syncPostgres(
    'auth',
    required('RAILWAY_AUTH_PG_URL'),
    'postgresql://postgres:root@localhost:5433/club_nautica_auth_db',
    ['roles', 'permisos', 'roles_permisos', 'usuarios', 'refresh_tokens']
  );

  console.log('=== SOCIOS (Postgres) ===');
  await syncPostgres(
    'socios',
    required('RAILWAY_SOCIOS_PG_URL'),
    'postgresql://postgres:postgres@localhost:5434/sociosDb',
    ['tipos_documento', 'socios', 'solicitudes', 'solicitudes_retiro']
  );

  console.log('=== NAUTICA (MySQL) ===');
  await syncMysql(
    'nautica',
    {
      host: required('RAILWAY_NAUTICA_MYSQL_HOST'),
      port: Number(required('RAILWAY_NAUTICA_MYSQL_PORT')),
      user: 'root',
      password: required('RAILWAY_NAUTICA_MYSQL_PASSWORD'),
      database: 'railway',
    },
    { host: 'localhost', port: 3307, user: 'root', password: 'root', database: 'club_nautica_db' },
    ['tripulantes', 'embarcaciones', 'radas', 'zarpes']
  );

  console.log('=== FACTURACION (SQL Server) ===');
  await syncMssql(
    'facturacion',
    {
      server: required('RAILWAY_FACTURACION_MSSQL_HOST'),
      port: Number(required('RAILWAY_FACTURACION_MSSQL_PORT')),
      user: 'sa',
      password: required('RAILWAY_FACTURACION_MSSQL_PASSWORD'),
      database: 'facturacionDb',
      options: { encrypt: true, trustServerCertificate: true },
    },
    {
      server: 'localhost',
      port: 1433,
      user: 'sa',
      password: 'ClubNautico2026!',
      database: 'facturacionDb',
      options: { encrypt: true, trustServerCertificate: true },
    },
    ['tarifas_servicios', 'disponibilidad_servicios', 'facturas', 'consumos', 'morosidad_intereses']
  );

  console.log('=== SINCRONIZACION COMPLETA ===');
}

main().catch((e) => { console.error('ERROR FATAL:', e); process.exit(1); });
