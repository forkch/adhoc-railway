'use strict';

/* Services */


// Demonstrate how to register ch.fork.AdHocRailway.services
// In this case it is a simple value service.
angular.module('myApp.ch.fork.AdHocRailway.services', []).
    value('version', '0.1').
    factory('socket', function ($rootScope) {
        var socket = io.connect('http://localhost:3000');
        socket.on('connect', function () { // TIP: you can avoid listening on `connect` and listen on events directly too!
            socket.emit('register', '123');
        });

        return {
            on: function (eventName, callback) {
                socket.on(eventName, function () {
                    var args = arguments;
                    $rootScope.$apply(function () {
                        callback.apply(socket, args);
                    });
                });
            },
            emit: function (eventName, data, callback) {
                socket.emit(eventName, data, function () {
                    var args = arguments;
                    $rootScope.$apply(function () {
                        if (callback) {
                            callback.apply(socket, args);
                        }
                    });
                })
            }
        };
    });
