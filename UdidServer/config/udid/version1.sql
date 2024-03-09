CREATE TABLE t_device_id
(
 seq LONG NOT NULL PRIMARY KEY,
 udid LONG NOT NULL,
 android_id LONG NOT NULL,
 widevine_id LONG NOT NULL,
 device_hash LONG NOT NULL,
 local_did CHAR(22) NOT NULL,
 create_time LONG NOT NULL,
 update_time LONG NOT NULL
)
#
CREATE UNIQUE INDEX idx_device_id_udid ON t_device_id(udid)
#
CREATE INDEX idx_device_id_android_id ON t_device_id(android_id)
#
CREATE INDEX idx_device_id_widevine_id ON t_device_id(widevine_id)
#
CREATE INDEX idx_device_id_device_hash ON t_device_id(device_hash)