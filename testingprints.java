package gitlet;
import java.util.Date;
import java.text.SimpleDateFormat;
public class testingprints {
    public static void main(String[] args){
        Date thisdate= new Date();
        SimpleDateFormat dateForm= new SimpleDateFormat("E LLL k:m:s y Z");
        System.out.println(dateForm.format(thisdate));
    }
}
