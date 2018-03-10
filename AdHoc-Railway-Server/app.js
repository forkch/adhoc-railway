var restify = require('restify');
var socketHandler = require('./socketio/socket.js');
var locomotiveApi = require('./rest/locomotiveRESTApi');
var turnoutApi = require('./rest/turnoutRESTApi');
var routeApi = require('./rest/routeRESTApi');
var mongoose = require('mongoose');


var Logger = require('bunyan');
var log = new Logger({
    name: 'helloapi',
    streams: [
        {
            stream: process.stdout,
            level: 'debug'
        },
        {
            path: 'hello.log',
            level: 'trace'
        }
    ],
    serializers: restify.bunyan.serializers
});


var server = restify.createServer({
    name: 'AdHoc-Railway-Server',
    log: log   // Pass our logger to restify.
});
server.use(restify.bodyParser());
server.use(restify.CORS());
server.use(restify.fullResponse());
server.pre(function (request, response, next) {
    request.log.info({req: request}, 'start');        // (1)
    return next();
});
server.on('after', function (req, res, route) {
    req.log.info({res: res}, "finished");             // (3)
});

function reqSerializer(req) {
    return {
        method: req.method,
        url: req.url,
        headers: req.headers
    }
}

server.use(restify.requestLogger({
    properties: {},
    serializers: {req: reqSerializer}
}));

var clients = {};

var io = require('socket.io')(server, {
    transports: [
    'websocket',
        'flashsocket',
        'htmlfile',
        'xhr-polling',
        'jsonp-polling',
        'polling'
    ],
        allowUpgrades: true,
        origins: '*:*'});

io.on('connection', function (client) {

    client.on('register', function (appId) {
        console.log("client registered with appId: " + appId);
        clients[appId] = client;
        console.log("Clients: ");
        Object.keys(clients).forEach(function (socketIOAppId) {
            console.log("   " + socketIOAppId);
        });
    });

    client.on('disconnect', function () {
    });
    return socketHandler(client);
});

function sendResponse(err, res, next, successCode, errCode, data) {
    if (!err) {
        res.send(successCode, data);
    } else {
        res.send(errCode, data);
    }
    next();
}
function sendDataToWebsocketClients(err, req, event, data) {
    if (err) {
        return;
    }
    var restCallAppId = req.headers['adhoc-railway-appid'];
    Object.keys(clients).forEach(function (socketIOAppId) {
        console.log("restAppId: " + restCallAppId + " --> socketIOAppId: " + socketIOAppId);
        if (restCallAppId !== socketIOAppId) {
            var socket = clients[socketIOAppId];
            socket.emit(event, data);
        }
    });
}
locomotiveApi.init(server, sendDataToWebsocketClients, sendResponse);
turnoutApi.init(server, sendDataToWebsocketClients, sendResponse);
routeApi.init(server, sendDataToWebsocketClients, sendResponse);

var Schema = mongoose.Schema, ObjectId = Schema.ObjectId;
mongoose.connect('mongodb://localhost/baehnle');

server.listen(3000, function () {
    console.log('%s listening at %s', server.name, server.url);
});

