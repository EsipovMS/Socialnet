delete from friendships;
delete from block_history;
delete from likes;
delete from notifications;
delete from messages;
delete from dialogs;
delete from post_comments;
delete from post2tag;
delete from post_files;
delete from posts;
delete from persons;

truncate table posts cascade;
truncate table persons cascade;

insert into persons (id,about,birth_date,change_password_token,configuration_code,deleted_time,email,first_name,is_approved,is_blocked,is_deleted,last_name,last_online_time,message_permissions,notifications_session_id,online_status,password,phone,photo,reg_date,city,country,telegram_id,person_settings_id) values (1,'About user','1972-11-14 21:25:19','xfolip091','1','2022-04-15 00:43:45','user1@email.com','Leon',true,false,false,'Kennedy','2022-07-21 14:45:29','adipiscing','ipsum','accumsan','$2a$10$DKfACXByOkjee4VELDw7R.BeslHcGeeLbCK2N8gV3.BaYjSClnObG','966-998-0544','go86atavdxhcvcagbv','2000-07-26 16:21:43','Bourg-en-Bresse','France',93,633);
insert into posts (is_blocked, is_deleted, post_text, time, time_delete, title, author_id) values (false, false, 'Post text', '2022-05-25 03:42:13', '2023-02-13 07:13:20', 'Post title #1', 1);
