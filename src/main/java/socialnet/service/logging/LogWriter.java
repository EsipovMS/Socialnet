package socialnet.service.logging;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

@Component
public class LogWriter extends TimerTask {

    private final AuthCloud authCloud = new AuthCloud();
    private final LogClean cleanLogsInCloud = new LogClean();

    @Bean
    public void writer() {

        Integer timeLoadingInCloud = 3_600_000;
        updateTimer(timeLoadingInCloud);
    }

    @Override
    public void run() {

        try {
            pushLogs(getHref());
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public String getHref() throws IOException {

        String path = createLogFile();
        URL url = new URL("https://cloud-api.yandex.net/v1/disk/resources/upload?path=" + path + "&overwrite=true");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", authCloud.getYandexToken());

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        String s = String.valueOf(content);
        String[] split = s.split("\"");
        String href = split[7];

        return href;
    }

    public void pushLogs(String href) throws IOException, ParseException {

        cleanLogsInCloud.deleteOldLogs();

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {

            File file = new File("logs.log");
            HttpPut httpPut = new HttpPut(href);
            httpPut.setEntity(new FileEntity(file));
            HttpResponse response = httpclient.execute(httpPut);
        }
    }

    public void updateTimer(Integer timeLoadingInCloud) {

        Timer time = new Timer();
        LogWriter writerLogsInCloud = new LogWriter();
        time.schedule(writerLogsInCloud, 0, timeLoadingInCloud);
    }

    public String createLogFile() {

        SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");
        String dateToday = DATE_FORMATTER.format(new Date());
        String pathLogFile = "log_" + dateToday + ".log";

        return pathLogFile;
    }

}
