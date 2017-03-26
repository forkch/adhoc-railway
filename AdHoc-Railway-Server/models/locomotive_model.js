var mongoose = require('mongoose');
var Schema = mongoose.Schema, ObjectId = Schema.ObjectId;

exports.LocomotiveSchema = new Schema({
    name: {type: String, index: true},
    bus: Number,
    address1: Number,
    address2: Number,
    mfxUUID : Number,
    type: String,
    image: String,
    imageBase64: String,
    description: String,
    groupId: ObjectId,
    functions: [
        {
            number: Number,
            description: String,
            isEmergencyBrakeFunction: Boolean,
            deactivationDelay: Number
        }
    ]

});

exports.LocomotiveSchema.options.toJSON = {
    transform: function (doc, ret, options) {
        ret.id = ret._id;
        delete ret._id;
        delete ret.__v;
        return ret;
    }
};

exports.LocomotiveGroupSchema = new Schema({
    name: String,
    locomotives: [ObjectId]
});
exports.LocomotiveGroupSchema.options.toJSON = {
    transform: function (doc, ret, options) {
        ret.id = ret._id;
        delete ret._id;
        delete ret.__v;
        return ret;
    }
};

exports.LocomotiveModel = mongoose.model('Locomotive', exports.LocomotiveSchema);
exports.LocomotiveGroupModel = mongoose.model('LocomotiveGroup', exports.LocomotiveGroupSchema);
