let mysql = require('mysql2');
connection = mysql.createConnection({
    host: 'localhost',
    user: 'capstone',
    database: 'tannae',
    password: 'zoqtmxhs17',
    port: 3306
});
connection = connection.promise();

async function calc() {
    let [result, field] = await connection.query('select * from Data');
    let total = 0, choi = 0, dong = 0, leee = 0;

    for (let i = 0; i < result.length; i++) {
        let data = result[i];
        let costo = JSON.parse(data.costo);
        let costn = JSON.parse(data.costn);

        let choiR = parseInt(costn['m00001'] / costo['m00001'] * 100);
        let dongR = parseInt(costn['m00002'] / costo['m00002'] * 100);
        let leeeR = parseInt(costn['m00003'] / costo['m00003'] * 100);

        total += choiR + dongR + leeeR;
        choi += choiR;
        dong += dongR;
        leee += leeeR;
    }

    total /= result.length * 3;
    choi /= result.length;
    dong /= result.length;
    leee /= result.length;

    console.log(`TOTAL : ${total}%\nCHOI : ${choi}%\nDONG : ${dong}\nLEEE : ${leee}%`);
}


calc();