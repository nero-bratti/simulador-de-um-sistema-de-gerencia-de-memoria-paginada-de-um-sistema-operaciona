public class App {
    public static void main(String[] args) throws Exception {
        String addressesPath = "addresses/virtual4bits.txt";
        Simulator simulator = new Simulator();
        simulator.run(addressesPath);
    }
}
