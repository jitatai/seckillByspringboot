package com.hnit.seckill.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hnit.seckill.domain.MiaoShaUser;

public class UserUtil {
	
	private static void createMiaoShaUser(int count) throws Exception{
		List<MiaoShaUser> MiaoShaUsers = new ArrayList<MiaoShaUser>(count);
		//生成用户
		for(int i=0;i<count;i++) {
			MiaoShaUser MiaoShaUser = new MiaoShaUser();
			MiaoShaUser.setId(13000000000L+i);
			MiaoShaUser.setLoginCount(1);
			MiaoShaUser.setNickname("MiaoShaUser"+i);
			MiaoShaUser.setRegisterDate(new Date());
			MiaoShaUser.setSalt("1a2b3c4d");
			MiaoShaUser.setPassword(MD5Util.inputPass2DBPass("123456", MiaoShaUser.getSalt()));
			MiaoShaUsers.add(MiaoShaUser);
		}
		System.out.println("create MiaoShaUser");
/*
//		//插入数据库
		Connection conn = DBUtil.getConn();
		String sql = "insert into miaosha_user(login_count, nickname, register_date, salt, password, id)values(?,?,?,?,?,?)";
		PreparedStatement pstmt = conn.prepareStatement(sql);
		for(int i=0;i<MiaoShaUsers.size();i++) {
			MiaoShaUser MiaoShaUser = MiaoShaUsers.get(i);
			pstmt.setInt(1, MiaoShaUser.getLoginCount());
			pstmt.setString(2, MiaoShaUser.getNickname());
			pstmt.setTimestamp(3, new Timestamp(MiaoShaUser.getRegisterDate().getTime()));
			pstmt.setString(4, MiaoShaUser.getSalt());
			pstmt.setString(5, MiaoShaUser.getPassword());
			pstmt.setLong(6, MiaoShaUser.getId());
			pstmt.addBatch();
		}
		pstmt.executeBatch();
		pstmt.close();
		conn.close();
		System.out.println("insert to db");
*/
		//登录，生成token
		String urlString = "http://localhost/do_login";
		File file = new File("D:/tokens.txt");
		if(file.exists()) {
			file.delete();
		}
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		file.createNewFile();
		raf.seek(0);
		for(int i=0;i<MiaoShaUsers.size();i++) {
			MiaoShaUser MiaoShaUser = MiaoShaUsers.get(i);
			URL url = new URL(urlString);
			HttpURLConnection co = (HttpURLConnection)url.openConnection();
			co.setRequestMethod("POST");
			co.setDoOutput(true);
			OutputStream out = co.getOutputStream();
			String params = "mobile="+MiaoShaUser.getId()+"&password="+MD5Util.inputPassFromPass("123456");
			out.write(params.getBytes());
			out.flush();
			InputStream inputStream = co.getInputStream();
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			byte buff[] = new byte[1024];
			int len = 0;
			while((len = inputStream.read(buff)) >= 0) {
				bout.write(buff, 0 ,len);
			}
			inputStream.close();
			bout.close();
			String response = new String(bout.toByteArray());
			JSONObject jo = JSON.parseObject(response);
			String token = jo.getString("data");
			System.out.println("create token : " + MiaoShaUser.getId());
			
			String row = MiaoShaUser.getId()+","+token;
			raf.seek(raf.length());
			raf.write(row.getBytes());
			raf.write("\r\n".getBytes());
			System.out.println("write to file : " + MiaoShaUser.getId());
		}
		raf.close();
		
		System.out.println("over");
	}
	
	public static void main(String[] args)throws Exception {
		createMiaoShaUser(5000);
	}
}
