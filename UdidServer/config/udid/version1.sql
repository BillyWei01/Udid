CREATE TABLE t_device_id
(
 seq LONG NOT NULL PRIMARY KEY,
 udid LONG NOT NULL,
 mac LONG NOT NULL,
 android_id LONG NOT NULL,
 serial_no LONG NOT NULL,
 physics_info LONG NOT NULL,
 dark_physics_info LONG NOT NULL,
 create_time LONG NOT NULL,
 update_time LONG NOT NULL
)
#
CREATE UNIQUE INDEX idx_device_id_udid ON t_device_id(udid)
#
CREATE INDEX idx_device_id_mac ON t_device_id(mac)
#
CREATE INDEX idx_device_id_android_id ON t_device_id(android_id)
#
CREATE INDEX idx_device_id_serial_no ON t_device_id(serial_no)
