'use strict';


// Declare app level module which depends on filters, and services
var app = angular.module('myApp', ['ui.bootstrap','myApp.filters', 'myApp.services', 'myApp.directives']).
  config(['$routeProvider', '$locationProvider', function($routeProvider, $locationProvider) {
    $routeProvider.when('/turnouts', {templateUrl: 'partials/turnouts', controller: TurnoutsCtrl});
    $routeProvider.when('/addTurnout/:groupId', {templateUrl: 'partials/addTurnout', controller: AddTurnoutCtrl});
    $routeProvider.when('/editTurnout/:id', {templateUrl: 'partials/editTurnout', controller: EditTurnoutCtrl});
    $routeProvider.when('/locomotives', {templateUrl: 'partials/locomotives', controller: LocomotivesCtrl});
    $routeProvider.otherwise({redirectTo: '/'});
    $locationProvider.html5Mode(true);
  }]);