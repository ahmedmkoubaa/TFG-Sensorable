import mysql from "mysql"
import debug from "debug"

const log = debug("database-client")

export interface QueryParams {
  query: string
  data?: any[]
  queryCallback: (err: mysql.MysqlError | null, rows: any) => void
}

export interface DatabaseManager {
  connect: () => void
  doQuery: (queryParams: QueryParams) => void
}

export function databaseManager(): DatabaseManager {
  const databaseConfig: mysql.ConnectionConfig = {
    host: process.env.DATABASE_HOST || "127.0.0.1",
    port: parseInt(process.env.DATABASE_PORT || "") || 3306,
    user: process.env.DATABASE_USER || "database-service",
    password: process.env.DATABASE_PASS || "12345678",
    database: process.env.DATABASE_NAME || "test",
  }

  log("called usedatabase")
  let database: mysql.Connection

  //  A function to create and use a connection to the database
  // this function also handles errors related to the disconnection and reconnection
  function connect() {
    database = mysql.createConnection(databaseConfig) // first attempt of creating a connection

    // Now connect to the database server and check if there was any error
    database.connect(function (err) {
      if (err) {
        // The server is either down or restarting (takes a while sometimes).
        console.log("error when connecting to db:", err)
        setTimeout(connect, 5000)
      }
    })

    database.on("error", function (err) {
      console.log("db error", err)
      if (err.code === "PROTOCOL_CONNECTION_LOST") {
        // Connection to the MySQL server is usually
        connect() // lost due to either server restart, or a
      } else {
        // connnection idle timeout (the wait_timeout
        throw err // server variable configures this)
      }
    })
  }

  function checkQueryErrors(err: mysql.MysqlError | null, msg?: string) {
    if (err) {
      throw new Error(`Error trying to do a query -> ${msg}: ${err}`)
    }
  }

  function doQuery(params: QueryParams) {
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
    connect,
    doQuery,
  }
}
