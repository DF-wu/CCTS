package tw.dfder.ccts.entity;


public class UrlTemplate {
    private static final String URLTEMPLATE_PACTDETAIL = "/pacts/provider/PROVIDERNAME/consumer/CONSUMERNAME/latest";
    private static final String URLTEMPLATE_ALLPACTS = "/pacts/latest";

    public static String getPactDetailURLPath(String providerName, String consumerName){
        return URLTEMPLATE_PACTDETAIL.replace("PROVIDERNAME", providerName).replace("CONSUMERNAME",consumerName);
    }

    public static final String getAllPactsURLPath(){
        return URLTEMPLATE_PACTDETAIL;
    }


}
