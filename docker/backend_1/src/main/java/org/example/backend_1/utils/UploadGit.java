package org.example.backend_1.utils;

import org.apache.commons.io.FileUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.kohsuke.github.*;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class UploadGit {
    public static String upload(String url) throws IOException, InterruptedException {
        // GitHub个人访问令牌
        String accessToken = System.getenv("GithubAccessToken");

        // GitHub用户名和仓库名称
        String username = "abysshome";
        String repositoryName = "vivo";

        // 本地HTML文件路径
        File htmlFile = new File(url);
        String htmlFileName = UUID.randomUUID().toString()+htmlFile.getName();

        // 初始化GitHub对象
        GitHub github = new GitHubBuilder().withOAuthToken(accessToken).build();

        // 获取或创建仓库
        GHRepository repo;
        try {
            repo = github.getRepository(username + "/" + repositoryName);
        } catch (GHFileNotFoundException e) {
            repo = github.createRepository(repositoryName)
                    .description("Repository for Beijing route map")
                    .autoInit(true) // 自动初始化仓库
                    .create();
        }

        // 读取HTML文件内容
        String content = FileUtils.readFileToString(htmlFile, "UTF-8");

        // 上传HTML文件到GitHub仓库
        repo.createContent()
                .content(content)
                .path(htmlFileName)
                .message("Add Beijing route map")
                .branch("main")
                .commit();

        try {
            // 启用GitHub Pages
            enableGitHubPages(accessToken, username, repositoryName);
            // 输出GitHub Pages URL
            String githubPagesUrl = String.format("https://%s.github.io/%s/%s", username, repositoryName, htmlFileName);
            System.out.println("Map is available at: " + githubPagesUrl);
            return githubPagesUrl;
        } catch (Exception e) {
            System.out.printf("https://%s.github.io/%s/%s%n", username, repositoryName, htmlFileName);
            return "https://%s.github.io/%s/%s%n".formatted( username, repositoryName, htmlFileName);
        }
    }

    private static void enableGitHubPages(String accessToken, String username, String repositoryName) throws IOException {
        String url = String.format("https://api.github.com/repos/%s/%s/pages", username, repositoryName);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPut request = new HttpPut(url);
            request.addHeader("Authorization", "token " + accessToken);
            request.addHeader("Accept", "application/vnd.github.switcheroo-preview+json");

            String json = "{\"source\":{\"branch\":\"main\",\"path\":\"/\"}}";
            StringEntity entity = new StringEntity(json);
            request.setEntity(entity);

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                System.out.println("GitHub Pages response: " + responseBody);
            }
        }
    }
}
