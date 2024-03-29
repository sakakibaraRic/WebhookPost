/*
 * Copyright 2002-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Controller
@SpringBootApplication
public class Main extends HttpServlet {

  @Value("${spring.datasource.url}")
  private String dbUrl;

  @Autowired
  private DataSource dataSource;

  public static void main(String[] args) throws Exception {
    SpringApplication.run(Main.class, args);
  }

  @RequestMapping("/")
  String index() {
    return "index";
  }

  @RequestMapping("/db")
  String db(Map<String, Object> model) {
    try (Connection connection = dataSource.getConnection()) {
      Statement stmt = connection.createStatement();
      stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (tick timestamp)");
      stmt.executeUpdate("INSERT INTO ticks VALUES (now())");
      ResultSet rs = stmt.executeQuery("SELECT tick FROM ticks");

      ArrayList<String> output = new ArrayList<String>();
      while (rs.next()) {
        output.add("Read from DB: " + rs.getTimestamp("tick"));
      }

      model.put("records", output);
      return "db";
    } catch (Exception e) {
      model.put("message", e.getMessage());
      return "error";
    }
  }

  // 変更通知関連処理ここから
  @RequestMapping("/WebhookSubscription")
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
	  String validationToken = request.getParameter("validationToken");
	  System.out.println("ContentType:"+request.getContentType());
	  if(validationToken != null) {
		  // バリデーショントークンが存在する場合はサブスクリプション作成検証
		  validationToken = URLDecoder.decode(validationToken, "UTF-8");
		  System.out.println(validationToken);

	      response.setContentType("text/plain");
	      response.setStatus(200);

		  PrintWriter out = response.getWriter();
		  // 検証のためデコードしたバリデーショントークンを返却
		  out.write(validationToken);
	  } else {
		  // バリデーショントークンが存在しない場合は実際の変更通知
		  BufferedReader reader = request.getReader();
		  Stream<String> lines = reader.lines();
		  String result = lines.collect(Collectors.joining(""));
		  System.out.println("変更通知");
		  System.out.println(result);
		  response.setContentType("text/plain");
		  response.setStatus(202);
	  }
  }
  // ここまで

  @Bean
  public DataSource dataSource() throws SQLException {
    if (dbUrl == null || dbUrl.isEmpty()) {
      return new HikariDataSource();
    } else {
      HikariConfig config = new HikariConfig();
      config.setJdbcUrl(dbUrl);
      return new HikariDataSource(config);
    }
  }

}
