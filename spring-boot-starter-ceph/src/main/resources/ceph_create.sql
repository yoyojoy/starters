CREATE TABLE `sy_file` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `file_name` varchar(200) DEFAULT NULL COMMENT '文件名称',
  `bucket_name` varchar(200) NOT NULL COMMENT '分区名称',
  `is_delete` tinyint(4) DEFAULT '0' COMMENT '''是否已删除(1:是 0:否)''',
  `object_name` varchar(200) NOT NULL COMMENT '对象名称',
  `file_md5` varchar(32) DEFAULT NULL COMMENT '文件MD5',
  `file_ext` varchar(20) DEFAULT NULL COMMENT '文件扩展名',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='文件表';

