package com.ktbigdata.mr;

import java.io.IOException;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import org.apache.hadoop.io.IntWritable;

import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapreduce.Job;

import org.apache.hadoop.mapreduce.Mapper;

import org.apache.hadoop.mapreduce.Reducer;

import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;

import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import org.apache.hadoop.util.GenericOptionsParser;

 

public class Dedup {

 

    //map将输入中的value复制到输出数据的key上，并直接输出

    public static class Map extends Mapper<Object,Text,Text,Text>{

        private static Text line=new Text();//每行数据

       

        //实现map函数

        public void map(Object key,Text value,Context context)

                throws IOException,InterruptedException{

            line=value;

            context.write(line, new Text(""));

        }

       

    }

   

    //reduce将输入中的key复制到输出数据的key上，并直接输出

    public static class Reduce extends Reducer<Text,Text,Text,Text>{

        //实现reduce函数

        public void reduce(Text key,Iterable<Text> values,Context context)

                throws IOException,InterruptedException{

            context.write(key, new Text(""));

        }

       

    }
    
    /**
     * 删除文件或者文件夹
     * @param conf
     * @param uri
     * @param filePath
     * @throws IOException
     */
    public static void delete(Configuration conf , String uri,String filePath) throws IOException {
           Path path = new Path(filePath);
           FileSystem fs = FileSystem.get(URI.create(uri), conf);
           fs.deleteOnExit(path);
           System.out.println("Delete: " + filePath);
           fs.close();
   }
   

    public static void main(String[] args) throws Exception{
    	
    	String uri="hdfs://192.168.100.141:9000/";   //hdfs 地址
    	FileSystem fs=FileSystem.get(new URI("hdfs://192.168.100.141:9000"),new Configuration(),"hadoop");//关于权限
        Configuration conf = new Configuration();

        //这句话很关键

//        conf.set("mapred.job.tracker", "192.168.1.2:9001");
//	    conf.set("fs.defaultFS", "hdfs://192.168.100.141:9000");  
//        conf.set("mapreduce.framework.name", "yarn");  
//        conf.set("yarn.resourcemanager.address", "192.168.100.141:8032");  
//        conf.set("mapred.remote.os", "Linux");  
        System.out.println("系统"+conf.get("mapred.remote.os"));
        
        conf.set("mapred.jar", "E://java//workspace//01_hdfs_example//Dedup.jar");
        delete(conf ,uri,"hdfs://192.168.100.141:9000/temp/dedup_out"); //删除文件夹
        
		String[] ioArgs = new String[] { "hdfs://192.168.100.141:9000/temp/dedup_in", "hdfs://192.168.100.141:9000/temp/dedup_out" };

		String[] otherArgs = new GenericOptionsParser(conf, ioArgs).getRemainingArgs();

		if (otherArgs.length != 2) {

			System.err.println("Usage: Dedup <in> <out>");

			System.exit(2);

		}

		Job job = new Job(conf, "Dedup");

     job.setJarByClass(Dedup.class);

     

     //设置Map、Combine和Reduce处理类

     job.setMapperClass(Map.class);

     job.setCombinerClass(Reduce.class);

     job.setReducerClass(Reduce.class);

     

     //设置输出类型

     job.setOutputKeyClass(Text.class);

     job.setOutputValueClass(Text.class);

     

     //设置输入和输出目录

     FileInputFormat.addInputPath(job, new Path(otherArgs[0]));

     FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));

     System.exit(job.waitForCompletion(true) ? 0 : 1);

     }

}