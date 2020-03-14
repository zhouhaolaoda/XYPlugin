var exec = require('cordova/exec');
var api = {};

//初始化
api.initXY = function (extId, success, error) {
    exec(success, error, 'XYPlugin', 'initXY', [extId]);
};

//匿名登录
api.loginExternal = function (displayName, externalUserId, success, error) {
    exec(success, error, 'XYPlugin', 'loginExternal', [displayName, externalUserId]);
};

//账号登录
api.loginXYlinkAccount = function (userName, passWord, success, error) {
    exec(success, error, 'XYPlugin', 'loginXYlinkAccount', [userName, passWord]);
};

//退出登录
api.loginOut = function (flag, success, error) {
    exec(success, error, 'XYPlugin', 'loginOut', [flag]);
};

//加入会议
api.joinMeeting = function (number, password, success, error) {
    exec(success, error, 'XYPlugin', 'joinMeeting', [number, password]);
};

//来电监听
api.inComeing = function (flag, success, error) {
    exec(success, error, 'XYPlugin', 'inComeing', [flag]);
};

//对外映射接口
module.exports = api;
