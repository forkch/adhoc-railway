'use strict';


// Declare app level module which depends on filters, and services
var app = angular.module('myApp', ['ui.bootstrap', 'myApp.services']).
  config(['$routeProvider', '$locationProvider', function($routeProvider, $locationProvider) {
    $routeProvider.when('/turnouts', {templateUrl: 'partials/turnouts', controller: TurnoutsCtrl});
    $routeProvider.when('/addTurnoutGroup', {templateUrl: 'partials/addTurnoutGroup', controller: AddTurnoutGroupCtrl});
    $routeProvider.when('/editTurnoutGroup/:id', {templateUrl: 'partials/editTurnoutGroup', controller: EditTurnoutGroupCtrl});
    $routeProvider.when('/addTurnout/:groupId', {templateUrl: 'partials/addTurnout', controller: AddTurnoutCtrl});
    $routeProvider.when('/editTurnout/:id', {templateUrl: 'partials/editTurnout', controller: EditTurnoutCtrl});
    
    $routeProvider.when('/routes', {templateUrl: 'partials/routes', controller: RoutesCtrl});
    $routeProvider.when('/addRouteGroup', {templateUrl: 'partials/addRouteGroup', controller: AddRouteGroupCtrl});
    $routeProvider.when('/editRouteGroup/:id', {templateUrl: 'partials/editRouteGroup', controller: EditRouteGroupCtrl});
    $routeProvider.when('/addRoute/:groupId', {templateUrl: 'partials/addRoute', controller: AddRouteCtrl});
    $routeProvider.when('/editRoute/:id', {templateUrl: 'partials/editRoute', controller: EditRouteCtrl});

    $routeProvider.when('/locomotives', {templateUrl: 'partials/locomotives', controller: LocomotivesCtrl});
    $routeProvider.when('/addLocomotiveGroup', {templateUrl: 'partials/addLocomotiveGroup', controller: AddLocomotiveGroupCtrl});
    $routeProvider.when('/editLocomotiveGroup/:id', {templateUrl: 'partials/editLocomotiveGroup', controller: EditLocomotiveGroupCtrl});
    $routeProvider.when('/addLocomotive/:groupId', {templateUrl: 'partials/addLocomotive', controller: AddLocomotiveCtrl});
    $routeProvider.when('/editLocomotive/:id', {templateUrl: 'partials/editLocomotive', controller: EditLocomotiveCtrl});

    $routeProvider.otherwise({redirectTo: '/'});
    $locationProvider.html5Mode(true);
  }]);	
