import mysql from "mysql";
export declare function useDatabase(): {
    init: () => void;
    checkInitialized: () => void;
    connect: () => void;
    doQuery: (query: string, queryCallback: (err: mysql.MysqlError | null, rows: any) => void) => void;
};
export declare function statrtDatabaseService(): void;
//# sourceMappingURL=index.d.ts.map