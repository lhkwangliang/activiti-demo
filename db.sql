create database activiti default character set utf8;

use activiti;

CREATE TABLE `ACT_ADD_SIGN` (
    `ID_` bigint(20) NOT NULL AUTO_INCREMENT,
    `PROCESS_DEFINITION_ID_` varchar(255) NOT NULL COMMENT '流程定义 ID',
    `ASSIGNEE_` varchar(32) NOT NULL COMMENT '操作人 ID',
    `ACT_ID_` varchar(64) NOT NULL COMMENT '活动 ID',
    `PROCESS_INSTANCE_ID_` varchar(255) NOT NULL COMMENT '流程实例 ID',
    `PROPERTIES_TEXT_` varchar(2000) DEFAULT NULL COMMENT '参数',
    `STATE_` int(11) DEFAULT NULL COMMENT '状态位, 0-有效, 1-无效',
    `CREATE_TIME_` bigint(20) DEFAULT NULL COMMENT '创建时间',
    PRIMARY KEY (`ID_`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

select * from ACT_ADD_SIGN;

select * from act_evt_log;
select * from act_ge_bytearray;
select * from act_ge_property;

select * from act_procdef_info;
select * from act_re_deployment;
select * from act_re_model;
select * from act_re_procdef;

select * from act_hi_actinst;
#select * from act_hi_attachment;
# select * from act_hi_comment;
# select * from act_hi_detail;
select * from act_hi_identitylink;
select * from act_hi_procinst;
select * from act_hi_taskinst;
select * from act_hi_varinst;

# select * from act_ru_deadletter_job;
# select * from act_ru_event_subscr;
select * from act_ru_execution;
select * from act_ru_identitylink;
# select * from act_ru_integration;
# select * from act_ru_job;
#select * from act_ru_suspended_job;
select * from act_ru_task;
# select * from act_ru_timer_job;
select * from act_ru_variable;