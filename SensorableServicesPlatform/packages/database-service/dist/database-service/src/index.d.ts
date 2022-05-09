import mysql from "mysql";
export interface QueryParams {
    query: string;
    data?: any[];
    queryCallback: (err: mysql.MysqlError | null, rows: any) => void;
}
export declare function useDatabase(): {
    init: () => void;
    checkInitialized: () => void;
    connect: () => void;
    doQuery: (params: QueryParams) => void;
};
export declare function statrtDatabaseService(): void;
//# sourceMappingURL=index.d.ts.map