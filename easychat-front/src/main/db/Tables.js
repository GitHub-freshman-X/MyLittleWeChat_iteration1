const add_tables = [
  "create table if not exists chat_message(" +
  "user_id varchar not null," +
  "message_id integer primary key autoincrement," +  // 改这里，去掉 default null
  "session_id varchar," +
  "message_type integer," +
  "message_content varchar," +
  "contact_type integer," +
  "send_user_id varchar," +
  "send_user_nick_name varchar," +
  "send_time bigint," +
  "status integer," +
  "file_size bigint," +
  "file_name varchar," +
  "file_path varchar," +
  "file_type integer" +
  ");",
  "create table if not exists chat_session_user(" +
  "user_id varchar not null default 0," +
  "contact_id varchar(11) not null," +
  "contact_type integer," +
  "session_id varchar(11)," +
  "status integer default 1," +
  "contact_name varchar(20)," +
  "last_message varchar(500)," +
  "last_receive_time bigint," +
  "no_read_count integer default 0," +
  "member_count integer," +
  "top_type integer default 0," +
  "primary key (user_id, contact_id)" +
  ");",
  "create table if not exists user_setting (" +
  "user_id varchar not null," +
  "email varchar not null," +
  "sys_setting varchar," +
  "contact_no_read integer," +
  "server_port integer," +
  "primary key(user_id)" +
  ");"
];

const alter_tables = [

];

const add_index = [
  "create index if not exists idx_session_id " +  // ✅这里加空格
  "on chat_message(" +
  "session_id asc" +
  ");"
];

export {
  add_tables,
  alter_tables,
  add_index
}
