package tw.broccoli.amybus;

/**
 * Created by broccoli on 15/2/7.
 */
public class Result {
    public int    status = NONE;
    public String message = "";

    public static final int NONE    = 0;
    public static final int SUCCESS = 1;
    /**流程有正常跑完，只是在邏輯上為false*/
    public static final int FAILED  = 2;
    /**流程異常*/
    public static final int ERROR   = 3;

    public Result() {
    }

    public Result(int status) {
        this(status, "");
    }

    public Result(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
