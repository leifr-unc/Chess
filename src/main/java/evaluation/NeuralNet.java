package evaluation;

// Stores a neural net, and all connections between nodes.  Allows for running inputs through the net.
// This net does not handle the machine learning.  It simply runs already trained nets.

public class NeuralNet {
    // Use ints, since they are faster than doubles
    // Just have the value be between 0 and 10,000, and then pretend that the value is 10,000 times smaller.

    private double[][] _layers; // The layers.  layers[0] is the first, aka input. layers[layers.length - 1] is output.
    private double[][][] _weights; // [a][b][c] ... a is connection from 0 = inputs, 1 = layers[0] etc.
    // b is the to node in a+1.
    // c is the from node in a.
    // value is the strength of the connection
    private double[][] _biases; // The biases ... [a][b] ... a is the network, b is the node in the network, value is bias.

    public NeuralNet(int[] sizes) {
        _layers = new double[sizes.length][];
        for (int i = 0; i < sizes.length; i++) {
            _layers[i] = new double[sizes[i]];
        }

        _weights = new double[sizes.length-1][][];
        _biases = new double[sizes.length - 1][];
        for (int i = 0; i < sizes.length-1; i++) {
            _weights[i] = new double[sizes[i+1]][];
            _biases[i] = new double[sizes[i+1]];
            for (int j = 0; j < sizes[i+1]; j++) {
                _weights[i][j] = new double[sizes[i]];
            }
        }
    }


    public NeuralNet(double[][][] weights, double[][] biases) {
        _weights = weights;
        _biases = biases;
        _layers = new double[biases.length + 1][];
        for (int i = 0; i < biases.length; i++) {
            _layers[i] = new double[weights[i][0].length];
        }
        _layers[_layers.length - 1] = new double[weights[weights.length-1].length];
//        System.out.println("layer 0 len: " + _layers[0].length);
//        System.out.println("layer 1 len: " + _layers[1].length);
//        System.out.println("layer 2 len: " + _layers[2].length);
    }

    public double[][] getLayers() {
        return _layers; // Not encapsulated, but very fast.
    }

    public double[][][] getWeights() {
        return _weights; // Again, not encapsulated, but very fast.
    }

    public double[][] getBiases() {
        return _biases; // Still not encapsulation, still fast.
    }

    public double[] execute(double[] inputs) {
        /* Takes in initial values for the nodes, and then runs the network through
         * until the end is reached.  Then, the last layer is returned.
         */
        _layers[0] = inputs.clone();
        for (int i = 1; i < _layers.length; i++) {
            // Each iteration fills out the ith layer with the values needed.
//            System.out.println("i = " + i);
//            System.out.println("layers[0].length: " + _layers[0].length);
            _layers[i] = getNextLayer(_layers[i-1], _weights[i-1], _biases[i-1]);
        }
        return _layers[_layers.length-1];
    }

    private double[] getNextLayer(double[] prevLayer, double[][] weights, double[] biases) {
        /* prevLayer contains the previous layer's values. */

        /* weights contains the values of the multipliers, where the
         *  > first index is the spot in the output.
         *  > second index is the spot in prevLayer.
         *  > value is the multiplier, or value of the connection (can be negative).
         */

        /* biases contains the values to subtract from the results in output after
         * they have been calculated, but before they have been sigmoided.
         */
        double[] output = new double[weights.length];
        for (int i = 0; i < output.length; i++) {
            // Runs for each output node.
            double outputI = 0.0;
            for (int j = 0; j < weights[i].length; j++) {
                // Runs for each input node connected to that output node.
                outputI += weights[i][j] * prevLayer[j];
            }
            outputI += biases[i];
            output[i] = sigmoid(outputI);
        }
        return output;
    }

    private double sigmoid(double input) {
        double eToInput = Math.pow(Math.E, input);
        return eToInput / (eToInput + 1);
    }
}