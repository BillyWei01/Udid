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
#
CREATE TABLE t_did_log
(
	md5 char(22) NOT NULL UNIQUE,
	udid LONG NOT NULL,
	mac char(17) NOT NULL,
	android_id char(16) NOT NULL,
	serial_no char(33) NOT NULL,
	physics_info LONG  NOT NULL ,
	dark_physics_info LONG NOT NULL,
	create_time LONG NOT NULL
)
#
CREATE INDEX idx_did_log_udid  ON t_did_log(udid)
#
CREATE INDEX idx_did_log_create_time  ON t_did_log(create_time)
#
CREATE TABLE t_install_log
(
install_id char(20) NOT NULL UNIQUE,
udid LONG NOT NULL,
create_time LONG NOT NULL
)
#
CREATE INDEX idx_install_log_udid ON t_install_log(udid)
#
CREATE INDEX idx_install_log_create_time  ON t_install_log(create_time)
