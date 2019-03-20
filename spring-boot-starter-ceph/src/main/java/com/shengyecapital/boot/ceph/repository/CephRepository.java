package com.shengyecapital.boot.ceph.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

@Repository
@Slf4j
public class CephRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final String INSERT_SQL = "insert into sy_file (file_name, bucket_name, object_name, file_md5, file_ext) VALUES (?,?,?,?,?)";
    private static final String QUERY_SQL_BYID = "select id,file_name,bucket_name,object_name,file_md5,file_ext from sy_file where id = ?";
    private static final String QUERY_SQL_BYKEY = "select id,file_name,bucket_name,object_name,file_md5,file_ext from sy_file where bucket_name = ? and object_name = ?";
    private static final String REMOVE_SQL_BYID = "update sy_file set is_delete = 1 where id = ?";
    private static final String REMOVE_SQL_BYKEY = "update sy_file set is_delete = 1 where bucket_name = ? and object_name = ?";

    public int insertFile(CephFile file){
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(Connection conn) throws SQLException {
                PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, file.getFileName());
                ps.setString(2, file.getBucketName());
                ps.setString(3, file.getObjectName());
                ps.setString(4, file.getFileMd5());
                ps.setString(5, file.getFileExt());
                return ps;
            }
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    public CephFile queryCephFileById(int id){
        return jdbcTemplate.queryForObject(QUERY_SQL_BYID, new Object[]{id},new BeanPropertyRowMapper<CephFile>(CephFile.class));
    }

    public CephFile queryCephFileByKey(String bucketName, String cephKey){
        return jdbcTemplate.queryForObject(QUERY_SQL_BYKEY, new Object[]{bucketName, cephKey}, new BeanPropertyRowMapper<CephFile>(CephFile.class));
    }

    public void remove(int fileId){
        jdbcTemplate.update(REMOVE_SQL_BYID, new Object[]{fileId});
    }

    public void remove(String bucketName, String cephKey){
        jdbcTemplate.update(REMOVE_SQL_BYKEY, new Object[]{bucketName, cephKey});
    }

}
