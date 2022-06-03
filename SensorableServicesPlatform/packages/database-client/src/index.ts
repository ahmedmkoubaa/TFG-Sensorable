import mysql from "mysql"
import debug from "debug"

const log = debug("database-client")

export interface QueryParams {
  query: string
  data?: any[]
  queryCallback: (err: mysql.MysqlError | null, rows: any) => void
}

export interface DatabaseManager {
  init: () => void
  checkInitialized: () => void
  connect: () => void
  doQuery: (queryParams: QueryParams) => void
}

export function databaseManager(): DatabaseManager {
  log("called usedatabase")
  let database: mysql.Connection

  function init() {
    database = mysql.createConnection({
      host: process.env.DATABASE_HOST || "127.0.0.1",
      port: parseInt(process.env.DATABASE_PORT || "") || 3306,
      user: process.env.DATABASE_USER || "database-service",
      password: process.env.DATABASE_PASS || "12345678",
      database: process.env.DATABASE_NAME || "test",
    })
  }

  function checkInitialized() {
    if (!database) {
      throw new Error("Error: using unintialized database, call init you use the mentioned database")
    }
  }

  function connect() {
    checkInitialized()

    database.connect((err: unknown) => {
      if (err) {
        log("Error: can't connect to database server, code: %o", err)
        throw err
      }

      log("SUCCESS DATABASE CONNECTION!")
    })
  }

  function checkQueryErrors(err: mysql.MysqlError | null, msg?: string) {
    if (err) {
      log("Error: while executing a query -> %o optional message is -> %s", err, msg)
      throw new Error("Error: trying to execute a query and handled the next error")
    }
  }

  function doQuery(params: QueryParams) {
    checkInitialized()

    if (params.data?.length == 0) {
      log("No params tu push")
      return
    }

    database.query(params.query, [params.data], (err, rows) => {
      checkQueryErrors(err)
      params.queryCallback(err, rows)
    })
  }

  return {
    init,
    checkInitialized,
    connect,
    doQuery,
  }
}
