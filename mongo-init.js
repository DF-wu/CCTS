db.createUser({
    user: "soselab", // 帳號
    pwd: "soselab401", // 密碼
    roles: [
        {
            role: "readWrite",
            db: "CCTS", // 授權的db
        },
    ],
});