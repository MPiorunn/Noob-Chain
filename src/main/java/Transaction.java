import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.LinkedList;

public class Transaction {
    public PublicKey getSender() {
        return sender;
    }

    public void setSender(PublicKey sender) {
        this.sender = sender;
    }

    public PublicKey getReceipient() {
        return receipient;
    }

    public void setReceipient(PublicKey receipient) {
        this.receipient = receipient;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public byte[] getSignature() {
        return signature;
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    public LinkedList<TransactionInput> getInputs() {
        return inputs;
    }

    public void setInputs(LinkedList<TransactionInput> inputs) {
        this.inputs = inputs;
    }

    public LinkedList<TransactionOutput> getOutputs() {
        return outputs;
    }

    public void setOutputs(LinkedList<TransactionOutput> outputs) {
        this.outputs = outputs;
    }

    public static int getSequence() {
        return sequence;
    }

    public static void setSequence(int sequence) {
        Transaction.sequence = sequence;
    }

    private String transactionId; // this is also the hash of the function

    private PublicKey sender; // senders address/public key
    private PublicKey receipient; // Recipients address/public key
    private float value;
    public byte[] signature; // this is to prevent anybody else from spending funds in our wallet
    public LinkedList<TransactionInput> inputs;

    public LinkedList<TransactionOutput> outputs = new LinkedList<>();
    private static int sequence = 0; // a rough count of how many transaction have been generated

    public Transaction(PublicKey sender, PublicKey receiver, float value, LinkedList<TransactionInput> inputs) {
        this.sender = sender;
        this.receipient = receiver;
        this.value = value;
        this.inputs = inputs;
    }


    public boolean processTransaction() {
        if (!verifySignature()) {
            System.out.println("Transaction Signature  failed to verify");
            return false;
        }
        for (TransactionInput input : inputs) {
            input.setUTXO(NoobChain.UTXOs.get(input.getTransactionOutputId()));
        }

        if (getInputsValue() < NoobChain.minimumTransaction) {
            System.out.println("#Transaction Inputs to small: " + getInputsValue());
            return false;
        }

        float leftOver = getInputsValue() - value;
        transactionId = calculateHash();
        outputs.add(new TransactionOutput(this.receipient, value, transactionId));
        outputs.add(new TransactionOutput(this.sender, leftOver, transactionId));
        for (TransactionOutput output : outputs) {
            NoobChain.UTXOs.put(output.getId(), output);
        }

        for (TransactionInput input : inputs) {
            if (input.getUTXO() == null) continue;
            NoobChain.UTXOs.remove(input.getTransactionOutputId());
        }
        return true;
    }

    public float getInputsValue() {
        float total = 0;
        for (TransactionInput input : inputs) {
            if (input.getUTXO() == null) continue;
            total += input.getUTXO().getValue();
        }
        return total;
    }

    public float getOutputsValue() {
        float total = 0;
        for (TransactionOutput output : outputs) {
            total += output.getValue();
        }
        return total;
    }

    /**
     * Signs all the data we don't wish to be tampered with
     *
     * @param privateKey - sender private key
     */
    public void generateSignature(PrivateKey privateKey) {
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(receipient);
        signature = StringUtil.applyECDSASig(privateKey, data);
    }

    /**
     * Verifies the ata we signed hasn't been tampered with
     *
     * @return result
     */
    public boolean verifySignature() {
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(receipient);
        return StringUtil.verifyECDSASIg(sender, data, signature);
    }

    private String calculateHash() {
        sequence++;
        return StringUtil.applySha256(
                StringUtil.getStringFromKey(sender) +
                        StringUtil.getStringFromKey(receipient) +
                        Float.toString(value) +
                        sequence);
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
