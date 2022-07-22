// <<< Service >>>

// << Require >>
let request = require('request');

// << Path Class >>
module.exports.Service = class Service {
    // < Construct Path >
    constructor(connection, socket, data) {
        // Basic setting
        this.pathR;
        this.connection = connection;
        this.socket = socket;
        this.data = data;
        // Set request path structure
        this.path = {
            "origin": { "name": "Vehicle", "x": 0, "y": 0 },
            "destination": { "name": null, "x": 0, "y": 0 },
            "waypoints": [],
            "priority": "DISTANCE", "car_fuel": "GASOLINE", "car_hipass": false, "alternatives": false, "road_details": false, "summary": true, "alternatives": true
        }
        // Set request
        this.pathReq = {
            headers: { 'content-type': 'application/json', 'authorization': 'KakaoAK d94b5c67305d6a10b3e43e5da881e7cf' },
            url: 'https://apis-navi.kakaomobility.com/v1/waypoints/directions',
            body: null,
            json: true
        }
    }

    // < Set vehicle >
    async setVehicle() {
        // Search vehicles available (share ? check available vehicles which is serving : check available vehicles which is on service but not serving)
        let sql = this.data.share ? `select * from Vehicle where state = true and num != 3 and num != 0 and share = 1 and gender = ${this.data.user.gender}` : `select * from Vehicle where state = true and num = 0`;
        let [vehicles, field] = await this.connection.query(sql);
        let nearestIndex = -1;
        let minDistance = Number.MAX_VALUE;
        this.flag = 0;      // 0 : No vehicle / 1 : Share vehicle / 2 : Non share vehicle for share user / 3 : Non share vehicle for non-share user
        this.pointss = [];  // Stores points for each vehicle (Unavailable : null, available : Point)
        this.starts = [];   // Stores startIndex
        this.ends = [];     // Stores endIndex

        // Select nearest vehicle which is available
        for (let i = 0; i < vehicles.length; i++) {
            // If share than check if requested path is available on vehicle's path
            if (this.data.share) {
                let [flagP, points, startIndex, endIndex] = this.checkInnerPath(vehicles[i]);
                this.pointss.push(points);
                this.starts.push(startIndex);
                this.ends.push(endIndex);
                // If not available than don't check current vehicle
                if (!flagP)
                    continue;
                this.flag = 1;
            }

            // Check current vehicle and see if is the nearest one
            let position = vehicles[i].pos.split(' ');
            let distance = Math.sqrt(Math.pow(this.data.start.x - position[0], 2) + Math.pow(this.data.start.y - position[1], 2));
            nearestIndex = minDistance > distance ? i : nearestIndex;
            minDistance = minDistance > distance ? distance : minDistance;
        }

        // Search if share request has no vehicle
        if (this.data.share && nearestIndex == -1) {
            [vehicles, field] = await this.connection.query(`select * from Vehicle where state = true and num = 0`);
            for (let i = 0; i < vehicles.length; i++) {
                let position = vehicles[i].pos.split(' ');
                let distance = Math.sqrt(Math.pow(this.data.start.x - position[0], 2) + Math.pow(this.data.start.y - position[1], 2));
                nearestIndex = minDistance > distance ? i : nearestIndex;
                minDistance = minDistance > distance ? distance : minDistance;
            }
        }

        // Set start index and end index
        if (this.flag === 1) {                          // When share vehicle is matched
            this.starts = this.starts[nearestIndex];
            this.ends = this.ends[nearestIndex];
        } else if (nearestIndex !== -1) {               // When non-share vehicle is matched
            this.starts = 1;
            this.ends = 2;
        }

        // Set vehicle
        if (nearestIndex !== -1) {
            this.vehicle = vehicles[nearestIndex];
            this.pointss = this.pointss[nearestIndex];
            this.flag = this.flag === 0 ? (this.data.share ? 2 : 3) : 1;
        } else
            this.vehicle = null;
    }

    // < Set path >
    setPath() {
        if (this.flag === 1) {
            let points = this.pointss;
            this.path.origin = points[0];
            this.path.destination = points[points.length - 1];
            this.path.waypoints = points.slice(1, points.length - 1);
        } else {
            this.path.origin.x = this.vehicle.pos.split(' ')[0];
            this.path.origin.y = this.vehicle.pos.split(' ')[1];
            this.path.destination = this.data.end;
            this.path.waypoints.unshift(this.data.start);
        }
        this.pathReq.body = this.path;
    }

    // < Request path info >
    reqPath() {
        return new Promise((resolve, reject) => {
            request.post(this.pathReq, (err, httpResponse, body) => {
                if (!err && httpResponse.statusCode == 200) resolve(body.routes[0]);
                else reject(err);
            });
        });
    }

    // < Check if user coordinates are available >
    checkInnerPath(vehicle) {
        // Setting
        let start = this.data.start;
        let end = this.data.end;
        let path = JSON.parse(vehicle.unpass);
        let points = path.waypoints;
        points.unshift(path.origin);
        points.push(path.destination);

        // Search starting point range
        let startIndex, endIndex, innerStart, innerEnd;
        for (let s = 0; s < points.length - 1; s++) {
            let prePoint = points[s];
            let postPoint = points[s + 1];
            innerStart = this.calculateInnerPoint(prePoint, start, postPoint);
            if (innerStart) {
                startIndex = s + 1;
                break;
            }
        }

        // Search ending point range
        for (let e = startIndex - 1; e < points.length - 1; e++) {
            let prePoint = points[e];
            let postPoint = points[e + 1];
            innerEnd = this.calculateInnerPoint(prePoint, end, postPoint);
            if (innerEnd) {
                endIndex = e + 1;
                break;
            }
        }

        // Check availability of new point
        if (innerStart && innerEnd) {
            // Get pre/post point
            let prePoint = points[startIndex - 1];
            let postPoint = points[endIndex];
            // Calculate distance (Prepoint)
            let preTOstart = Math.pow(prePoint.x - start.x, 2) + Math.pow(prePoint.y - start.y, 2);
            let preTOend = Math.pow(prePoint.x - end.x, 2) + Math.pow(prePoint.y - end.y, 2);
            // Calculate distance (Postpoint)
            let startTOpost = Math.pow(postPoint.x - start.x, 2) + Math.pow(postPoint.y - start.y, 2);
            let endTOpost = Math.pow(postPoint.x - end.x, 2) + Math.pow(postPoint.y - end.y, 2);
            // Return (When start point is far than end point) ? true : false
            if (startIndex == endIndex && !(preTOstart < preTOend && startTOpost > endTOpost))
                return [false, null, null, null];
            else {
                points.splice(endIndex, 0, end);
                points.splice(startIndex, 0, start);
                return [true, points, startIndex, endIndex + 1];
            }
        } else if (innerStart) {
            // Get last point and pre last point
            let lastPoint = points[points.length - 1];
            let preLastPoint = points[points.length - 2];
            // Get vector of prelast->last and last->end
            let uva = [lastPoint.x - preLastPoint.x, lastPoint.y - preLastPoint.y];
            let uvb = [end.x - lastPoint.x, end.y - lastPoint.y];
            // Get angle between prelast->last asd last->end
            let theta = Math.acos((uva[0] * uvb[0] + uva[1] * uvb[1]) / (Math.sqrt(Math.pow(uva[0], 2) + Math.pow(uva[1], 2)) * Math.sqrt(Math.pow(uvb[0], 2) + Math.pow(uvb[1], 2))));
            // Available when angle is smaller than 45 degrees
            if (theta < Math.PI / 4) {
                points.push(end);
                points.splice(startIndex, 0, start);
                return [true, points, startIndex, points.length];
            } else
                return [false, null, null, null];
        } else
            return [false, null, null, null];
    }

    // < Calculate if coordinate is in single path >
    calculateInnerPoint(prePoint, point, postPoint) {
        // Set vectors
        let preTOpostV = [postPoint.x - prePoint.x, postPoint.y - prePoint.y];
        let postTOpreV = [prePoint.x - postPoint.x, prePoint.y - postPoint.y];
        let preTOpointV = [point.x - prePoint.x, point.y - prePoint.y];
        let postTOpointV = [point.x - postPoint.x, point.y - postPoint.y];

        // Available when angle is smaller than 30 degrees
        let preTheta = Math.acos((preTOpostV[0] * preTOpointV[0] + preTOpostV[1] * preTOpointV[1]) / (Math.sqrt(Math.pow(preTOpostV[0], 2) + Math.pow(preTOpostV[1], 2)) * Math.sqrt(Math.pow(preTOpointV[0], 2) + Math.pow(preTOpointV[1], 2))));
        let postTheta = Math.acos((postTOpreV[0] * postTOpointV[0] + postTOpreV[1] * postTOpointV[1]) / (Math.sqrt(Math.pow(postTOpreV[0], 2) + Math.pow(postTOpreV[1], 2)) * Math.sqrt(Math.pow(postTOpointV[0], 2) + Math.pow(postTOpointV[1], 2))));
        return preTheta < Math.PI / 6 && postTheta < Math.PI / 6 ? true : false;
    }

    // < Update Database >
    async updateDB() {
        let summary = this.pathR.summary;
        let sections = this.pathR.sections;
        this.path = {
            "origin": this.path.origin,
            "destination": this.path.destination,
            "waypoints": this.path.waypoints,
            "distance": summary.distance,
            "duration": summary.duration,
            "sections": sections
        }

        let start = this.path.waypoints[this.starts - 1];
        let end = this.path.waypoints.length > this.ends - 1 ? this.path.waypoints[this.ends - 1] : this.path.destination;

        let names = this.flag === 1 ? JSON.parse(this.vehicle.names) : {};
        names[`${start.x}_${start.y}_${start.name}`] = { 'usn': this.data.user.usn, 'type': 'start' };
        names[`${end.x}_${end.y}_${end.name}`] = { 'usn': this.data.user.usn, 'type': 'end' };

        await this.connection.query(`update Vehicle set num = ${this.vehicle.num + 1}, unpass = '${JSON.stringify(this.path)}', share = ${this.data.share}, gender = ${this.data.user.gender}, cost = ${summary.fare.taxi}, names = '${JSON.stringify(names)}' where vsn = '${this.vehicle.vsn}'`);
        await this.connection.query(`update User set state = true where usn = '${this.data.user.usn}'`);
    }
}