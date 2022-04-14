// <<< Server >>>

// << Settings >>
// < Import >
let mysql = require('mysql');
let express = require('express');
let app = express();
let bodyParser = require('body-parser');

// < Uses >
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended: true}));

// < MySQL Connection >
let connection = mysql.createConnection({
    host: 'localhost',
    user: 'capstone',
    database: 'tannae',
    password: 'zoqtmxhs17',
    port: 3306
});

////update

// << Reqeust & Response >>
// < Account >
app.post('/user/login', (req, res) => {
    console.log('Login Request');

    let id = req.body.nameValuePairs.id
    let pw = req.body.nameValuePairs.pw
    let sql = 'select * from User where binary  id = ?';

    connection.query(sql, id, function (err, result) {
        let jsErr = {"error": "false"};

        if(err) {
            console.log(err)
        } else {
            if(result.length === 0) {
                //str = '[{"error":"Not a user"}]';
                jsErr.error = "Not a user";
                console.log('/user/login : Not a user');
            } else if(pw !== result[0].pw.toString('utf-8')) {
                jsErr.error = "Password mismatch";
                //str = '[{"error":"Password mismatch"}]';
                console.log('/user/login : Password mismatch');
            } else {
                console.log('/user/login : Login success');
            }
        }
        result.unshift(jsErr);
        res.json(JSON.stringify(result))
    });
});

app.listen(3000, () => {
    console.log('Listening on port 3000');
});
