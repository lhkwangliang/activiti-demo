package com.walker.activiti.mapper;

import com.walker.activiti.entity.AddSign;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface AddSignMapper {

    @Select("select * from ACT_ADD_SIGN where STATE_ = 0 AND PROCESS_INSTANCE_ID_ = #{processInstanceId}")
    @Results({
            @Result(property = "id", column = "ID_"),
            @Result(property = "processDefinitionId", column = "PROCESS_DEFINITION_ID_"),
            @Result(property = "assignee", column = "ASSIGNEE_"),
            @Result(property = "processInstanceId", column = "PROCESS_INSTANCE_ID_"),
            @Result(property = "propertiesText", column = "PROPERTIES_TEXT_"),
            @Result(property = "state", column = "STATE_"),
            @Result(property = "createTime", column = "CREATE_TIME_"),
    })
    List<AddSign> find(String processInstanceId);

    @Insert("insert into ACT_ADD_SIGN(PROCESS_DEFINITION_ID_, ASSIGNEE_, ACT_ID_, PROCESS_INSTANCE_ID_, PROPERTIES_TEXT_, STATE_, CREATE_TIME_) values(#{processDefinitionId}, #{assignee}, #{activityId}, #{processInstanceId}, #{propertiesText}, #{state}, #{createTime})")
    int insert(AddSign addSign);
}
