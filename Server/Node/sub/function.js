const mysql = require('mysql2');
const fs = require('fs');

// << MySQL Connection >>
connection = mysql.createConnection({
    host: 'localhost',
    user: 'capstone',
    database: 'tannae',
    password: 'zoqtmxhs17',
    port: 3306
});
connection = connection.promise();

// Create vehicles
function vechile() {
    for (let i = 0; i < 100; i++) {
        let vsn = 'v';
        for (let j = 0; j < 5 - (i + 1).toString().length; j++)
            vsn += '0';
        vsn += (i + 1).toString();
        connection.query(`insert Vehicle values('${vsn}', false, '126.96913490121608 37.55701346877968', 'temp${vsn}', 0, null, null, null, null, null, null, null)`);
    };
};

// Create drivers       100
function driver() {
    for (let i = 0; i < 100; i++) {
        let usn = 'u';
        for (let j = 0; j < 5 - (i + 1).toString().length; j++)
            usn += '0';
        usn += (i + 1).toString();
        usnNum = usn.replace('u', '');
        connection.query(`insert User values('${usn}', 'driverid${usnNum}', 'driverpw${usnNum}', 'driver${usnNum}', '980000-00${usnNum}', true, '010000${usnNum}', 'e${usnNum}@naver.com', true, 100000, 5.0, false)`);
    };
};

// Create Passengers    100
function pass() {
    for (let i = 100; i < 200; i++) {
        let usn = 'u';
        for (let j = 0; j < 5 - (i + 1).toString().length; j++)
            usn += '0';
        usn += (i + 1).toString();
        usnNum = usn.replace('u', '');
        connection.query(`insert User values('${usn}', 'passid${usnNum}', 'passpw${usnNum}', 'pass${usnNum}', '980000-00${usnNum}', true, '010000${usnNum}', 'e${usnNum}@naver.com', 0, 100000, 5.0, false)`);
    };
};

// Match Vehicle and Drivers
function match() {
    try {
        for (let i = 0; i < 100; i++) {
            let usn = 'u';
            for (let j = 0; j < 5 - (i + 1).toString().length; j++)
                usn += '0';
            usn += (i + 1).toString();
            vsn = usn.replace('u', 'v');
            connection.query(`update Vehicle set usn = '${usn}' where vsn = '${vsn}'`);
        };
    } catch (err) {
        console.log(err);
    }
};

// Set vehicle coordinate
function veco() {
    fs.readFile('coordinate.txt', 'utf8', async (err, data) => {
        try {
            let [result, field] = await connection.query('select vsn from Vehicle')
            let dat = data.split("\r\n");
            for (let i = 0; i < dat.length; i++) {
                let da = dat[i].split(",");
                connection.query(`update Vehicle set pos = '${da[2]} ${da[1]}' where vsn = '${result[i].vsn}'`);
            };
        } catch (errs) {
            console.log(errs);
        };
    });
};

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

// Main
switch(process.argv[2]) {
    case 'vehicle': vechile(); break;
    case 'driver': driver(); break;
    case 'pass': user(); break;
    case 'match': match(); break;
    case 'veco': veco(); break;
    case 'calc': calc(); break;
    default: console.log('default');
};