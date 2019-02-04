import java.security.Security;
import java.util.HashMap;
import java.util.LinkedList;

public class NoobChain {

    private static LinkedList<Block> blockchain = new LinkedList<>();
    static HashMap<String, TransactionOutput> UTXOs = new HashMap<>();
    private static int difficulty = 3;
    static float minimumTransaction = 0.1f;
    public static Wallet walletA;
    public static Wallet walletB;

    private static Transaction genesisTransaction;

    public static void main(String[] args) {
        //Setup Bouncey castle as a Security Provider
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        //Create wallets:
        walletA = new Wallet();
        walletB = new Wallet();
        Wallet coinbase = new Wallet();

        genesisTransaction = new Transaction(coinbase.getPublicKey(), walletA.getPublicKey(), 100f, null);
        genesisTransaction.generateSignature(coinbase.getPrivateKey());
        genesisTransaction.setTransactionId("0");
        genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.getReceipient(), genesisTransaction.getValue(), genesisTransaction.getTransactionId()));
        UTXOs.put(genesisTransaction.outputs.getFirst().getId(), genesisTransaction.outputs.getFirst());

        System.out.println("Creating and mining genesis block....");
        Block genesis = new Block("0");
        genesis.addTransaction(genesisTransaction);
        addBlock(genesis);

        Block block1 = new Block(genesis.getHash());
        System.out.println("WalletA balance is : " + walletA.getBalance());
        System.out.println("WalletA is attemting to send funds (40) to WalletB...");
        block1.addTransaction(walletA.sendFunds(walletB.getPublicKey(), 40f));
        addBlock(block1);
        System.out.println("WalletA balance is : " + walletA.getBalance());
        System.out.println("WalletB balance is : " + walletB.getBalance());

        Block block2 = new Block(block1.getHash());
        System.out.println("WalletA attemping to send more funds (1000) then it has...");
        block2.addTransaction(walletA.sendFunds(walletB.getPublicKey(), 1000f));
        addBlock(block2);
        System.out.println("WalletA balance is : " + walletA.getBalance());
        System.out.println("WalletB balance is : " + walletB.getBalance());

        Block block3 = new Block(block2.getHash());
        System.out.println("WalletB attemping to send more funds (20) to WalletA...");
        block3.addTransaction(walletB.sendFunds(walletA.getPublicKey(), 20f));
        addBlock(block3);
        System.out.println("WalletA balance is : " + walletA.getBalance());
        System.out.println("WalletB balance is : " + walletB.getBalance());
        isChainValid();
    }

    public static void addBlock(Block newBlock) {
        newBlock.mineBlock(difficulty);
        blockchain.add(newBlock);
    }

    public static boolean isChainValid() {
        Block currentBlock;
        Block previousBlock;
        String hashTarget = "0".repeat(difficulty);
        HashMap<String, TransactionOutput> tempUTXOs = new HashMap<>(); //a temporary working list of unspent transactions at a given block state.
        tempUTXOs.put(genesisTransaction.outputs.get(0).getId(), genesisTransaction.outputs.get(0));

        for (int i = 1; i < blockchain.size(); i++) {
            currentBlock = blockchain.get(i);
            previousBlock = blockchain.get(i - 1);
            if (!currentBlock.getHash().equals(currentBlock.calculateHash())) {
                System.out.println("Current hashes are not equal!");
                return false;
            }
            if (!previousBlock.getHash().equals(currentBlock.getPreviousHash())) {
                System.out.println("Previous hashes are not equal!");
                return false;
            }
            if (!currentBlock.getHash().substring(0, difficulty).equals(hashTarget)) {
                System.out.println("This block hasn't been mined!");
                return false;
            }
            TransactionOutput tempOutput;
            for (int t = 0; t < currentBlock.getTransactions().size(); t++) {
                Transaction currentTransaction = currentBlock.getTransactions().get(t);

                if (!currentTransaction.verifySignature()) {
                    System.out.println("#Signature on Transaction(" + t + ") is Invalid");
                    return false;
                }
                if (currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
                    System.out.println("#Inputs are note equal to outputs on Transaction(" + t + ")");
                    return false;
                }

                for (TransactionInput input : currentTransaction.inputs) {
                    tempOutput = tempUTXOs.get(input.getTransactionOutputId());

                    if (tempOutput == null) {
                        System.out.println("#Referenced input on Transaction(" + t + ") is Missing");
                        return false;
                    }

                    if (input.getUTXO().getValue() != tempOutput.getValue()) {
                        System.out.println("#Referenced input Transaction(" + t + ") value is Invalid");
                        return false;
                    }

                    tempUTXOs.remove(input.getTransactionOutputId());
                }

                for (TransactionOutput output : currentTransaction.outputs) {
                    tempUTXOs.put(output.getId(), output);
                }

                if (currentTransaction.outputs.get(0).getReciepient() != currentTransaction.getReceipient()) {
                    System.out.println("#Transaction(" + t + ") output reciepient is not who it should be");
                    return false;
                }
                if (currentTransaction.outputs.get(1).getReciepient() != currentTransaction.getSender()) {
                    System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
                    return false;
                }

            }

        }
        System.out.println("Blockchain is valid");
        return true;
    }
}
