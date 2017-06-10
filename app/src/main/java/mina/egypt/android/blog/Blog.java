package mina.egypt.android.blog;

/**
 * Created by lenovov on 10/14/2016.
 */
public class Blog {
    private String title ;
    private String desc ;
    private String image ;
    private String username;


    public Blog (){

    }

    public Blog(String title, String desc, String image) {
        this.title = title;
        this.desc = desc;
        this.image = image;
    }

    public String getDesc() {

        return desc;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
