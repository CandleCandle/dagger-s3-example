package uk.me.candle.dagger.example;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import java.net.MalformedURLException;
import java.net.URL;
import javax.inject.Inject;

public class Main {

    public static void main(String[] args) {
        MainComponent t = DaggerMainComponent
                .create();
        t.s3Lister().run();
    }
}

@Component(modules = {S3DepsModule.class})
interface MainComponent {
    S3Lister s3Lister();
}

class S3Lister implements Runnable {
    private final AmazonS3 s3Client;
    private final BucketPrinter bucketPrinter;

    @Inject
    public S3Lister(AmazonS3 s3Client, BucketPrinter bucketPrinter) {
        this.s3Client = s3Client;
        this.bucketPrinter = bucketPrinter;
    }

    @Override
    public void run() {
        for (Bucket b : s3Client.listBuckets()) {
            bucketPrinter.printBucket(b);
        }
    }
}

class BucketPrinter {
    @Inject public BucketPrinter() { }

    void printBucket(Bucket b) {
        System.out.println(b.getCreationDate() + " " + b.getName());
    }
}

@Module(includes = {AwsDepsModule.class})
class S3DepsModule {
    @Provides
    AmazonS3 provideS3Client(ClientConfiguration clientConfiguration) {
        return new AmazonS3Client(clientConfiguration);
    }
}

@Module
class AwsDepsModule {
    @Provides
    ClientConfiguration provideClientConfiguration() {
        ClientConfiguration conf = new ClientConfiguration();
        String[] envVars = {"https_proxy", "http_proxy"};
        for (String var : envVars) {
            String proxy;
            if ((proxy = System.getenv(var)) != null) {
                try {
                    String p = proxy.startsWith("http") ? proxy : "http://" + proxy;
                    URL u = new URL(p);
                    conf.setProxyHost(u.getHost());
                    conf.setProxyPort(u.getPort());
                    return conf;
                } catch (MalformedURLException ex) {
                    throw new IllegalArgumentException("Unable to understand the environment variable " + var + " with the value of " + proxy, ex);
                }
            }
        }
        return conf;
    }
}
