package surveilance.fish.publisher;

public class App {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Starting image producer...");
        new ImageProducer(new RsaEncrypter(), new AesEncrypter(), new AesUtil()).start();
    }
}
