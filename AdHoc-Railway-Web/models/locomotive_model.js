var mongoose = require('mongoose');
var Schema = mongoose.Schema, ObjectId = Schema.ObjectId;

exports.LocomotiveSchema = new Schema({
	name: {type: String, index: true},
	bus: Number,
	address: Number,
	type: String,
	image: String,
	description: String,
	group:  ObjectId

});

exports.LocomotiveGroupSchema = new Schema({
	name: String,
	locomotives : [ObjectId]
});


exports.LocomotiveModel = mongoose.model('Locomotive', exports.LocomotiveSchema);
exports.LocomotiveGroupModel = mongoose.model('LocomotiveGroup', exports.LocomotiveGroupSchema);
