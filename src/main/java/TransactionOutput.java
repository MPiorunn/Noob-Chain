import java.security.PublicKey;

public class TransactionOutput {
    private String id;
    private PublicKey reciepient; //aka new owner of these coins
    private float value; // the amount of coins they own
    private String parentTransactionId; // the id of the transaction this output was created in

    public TransactionOutput(PublicKey reciepient, float value, String parentTransactionId) {
        this.reciepient = reciepient;
        this.value = value;
        this.parentTransactionId = parentTransactionId;
        this.id = StringUtil.applySha256(StringUtil.getStringFromKey(reciepient) + Float.toString(value) + parentTransactionId);
    }

    public boolean isMine(PublicKey p) {
        return p == reciepient;
    }

    public String getId() {
        return id;
    }

    public PublicKey getReciepient() {
        return reciepient;
    }

    public float getValue() {
        return value;
    }

    public String getParentTransactionId() {
        return parentTransactionId;
    }
}

