package com.poetry.servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.ProgressListener;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.poetry.entity.Record;
import com.poetry.entity.User;
import com.poetry.service.RecordService;
import com.poetry.service.impl.RecordServiceImpl;

@WebServlet("/recordUpload")
public class RecordUpload extends HttpServlet {
	private RecordService recordService = new RecordServiceImpl();
	
	public void doPost(final HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		Record record = new Record();
		//得到上传文件的保存目录，将上传的文件存放于WEB-INF目录下，不允许外界直接访问，保证上传文件的安全
        String savePath = request.getSession().getServletContext().getRealPath("/upload");
        //上传时生成的临时文件保存目录
        String tempPath = request.getSession().getServletContext().getRealPath("/temp");
        File tmpFile = new File(tempPath);
        if (!tmpFile.exists()) {
            //创建临时目录
            tmpFile.mkdir();
        }
        
        //消息提示
        String message = "";
        try{
            //使用Apache文件上传组件处理文件上传步骤：
            //1、创建一个DiskFileItemFactory工厂
            DiskFileItemFactory factory = new DiskFileItemFactory();
            //设置工厂的缓冲区的大小，当上传的文件大小超过缓冲区的大小时，就会生成一个临时文件存放到指定的临时目录当中。
            factory.setSizeThreshold(1024*100);//设置缓冲区的大小为100KB，如果不指定，那么缓冲区的大小默认是10KB
            //设置上传时生成的临时文件的保存目录
            factory.setRepository(tmpFile);
            //2、创建一个文件上传解析器
            ServletFileUpload upload = new ServletFileUpload(factory);
            //监听文件上传进度
            final DecimalFormat df = new DecimalFormat("#00.0");
            upload.setProgressListener(new ProgressListener(){
                public void update(long pBytesRead, long pContentLength, int arg2) {
                    System.out.println("文件大小为：" + pContentLength + ",当前已处理：" + pBytesRead);
                    /** 可扩展 上传进度
                     */
                    double percent= (double)pBytesRead*100/(double)pContentLength;  
                    System.out.println(df.format(percent));  
                    request.getSession().setAttribute("UPLOAD_PERCENTAGE", df.format(percent));  
                }
            });
             //解决上传文件名的中文乱码
            upload.setHeaderEncoding("UTF-8"); 
            //3、判断提交上来的数据是否是上传表单的数据
            if(!ServletFileUpload.isMultipartContent(request)){
                //按照传统方式获取数据
                return;
            }
            
            //设置上传单个文件的大小的最大值，目前是设置为1024*1024*10字节，也就是10mb
            long data_unit = 1024;
            
            upload.setFileSizeMax(data_unit*data_unit*10);
            //设置上传文件总量的最大值，最大值=同时上传的多个文件的大小的最大值的和，目前设置为10GB
            upload.setSizeMax(data_unit*data_unit*20);
            //4、使用ServletFileUpload解析器解析上传数据，解析结果返回的是一个List<FileItem>集合，每一个FileItem对应一个Form表单的输入项
            List<FileItem> list = upload.parseRequest(request);
            String filename = null;
            for(FileItem item : list){
                //如果fileitem中封装的是普通输入项的数据
                if(item.isFormField()){
                    String name = item.getFieldName();
                    //解决普通输入项的数据的中文乱码问题
                    String value = item.getString("UTF-8");
                    if ("phoneNumber".equals(name)) {
                    	record.setPhoneNumber(value);
                    } else if ("poetryId".equals(name)) {
                    	record.setPoetryId(value);
                    } else if ("poetryTitle".equals(name)) {
                    	value = URLDecoder.decode(value,"UTF-8");
                    	record.setPoetryTitle(value);
                    }
                } else {	//如果fileitem中封装的是上传文件
                    filename = item.getName();	//得到上传的文件名称，
                    record.setRecordPath(filename);
                    if(filename == null || filename.trim().equals("")){
                        continue;
                    }
                    
                    //获取item中的上传文件的输入流
                    InputStream in = item.getInputStream();
                    //创建一个文件输出流
                    FileOutputStream out = new FileOutputStream(savePath + "\\" + filename);
                    //创建一个缓冲区
                    byte buffer[] = new byte[1024];
                    //判断输入流中的数据是否已经读完的标识
                    int len = 0;
                    //循环将输入流读入到缓冲区当中，(len=in.read(buffer))>0就表示in里面还有数据
                    while((len=in.read(buffer))>0){
                        //使用FileOutputStream输出流将缓冲区的数据写入到指定的目录(savePath + "\\" + filename)当中
                        out.write(buffer, 0, len);
                    }
                    //关闭输入流
                    in.close();
                    //关闭输出流
                    out.close();
                }
            }
            if (recordService.insert(record) > 0) {
            	message = "上传成功！";
            } else {
            	File f = new File(savePath + "\\" + filename);
            	f.delete();
            	message = "上传失败！";
            }
        }catch (FileUploadBase.FileSizeLimitExceededException e) {
            e.printStackTrace();
            request.setAttribute("message", "单个文件超出最大值！！！");
            request.getRequestDispatcher("/message.jsp").forward(request, response);
        }catch (FileUploadBase.SizeLimitExceededException e) {
            e.printStackTrace();
            request.setAttribute("message", "上传文件的总的大小超出限制的最大值！！！");
        }catch (Exception e) {
            message= "上传失败！未知错误" + e.toString();
            e.printStackTrace();
        }
        response.getOutputStream().write(message.getBytes("UTF-8"));
	}
}
