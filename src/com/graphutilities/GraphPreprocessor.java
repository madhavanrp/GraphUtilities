package com.graphutilities;



import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class GraphPreprocessor {

    private int[][] graph;
    private int[][] graphTranspose;

    private static final String GRAPH_ROOT_PATH = "graphs";
    private static final String OUTPUT_PATH = "output_graphs";
    private int n;
    private int m;
    private int inDegree[];

    public GraphPreprocessor(String fileName) {
        //Build path of graph
        Path currentRelativePath = Paths.get("");
        String baseProjectPath = currentRelativePath.toAbsolutePath().toString();
        String graphFilePath = baseProjectPath + File.separator + GRAPH_ROOT_PATH + File.separator + fileName;

        //Do an initial pass
        getVerticesAndEdges(graphFilePath);

        //Read the graph
        readGraph(graphFilePath);


        String outputPath = String.format(baseProjectPath + File.separator + OUTPUT_PATH);
//        assertTransposeIsCorrect(this.graph, this.graphTranspose);

//        writeToFile(outputPath, fileName);
        doGraphOperations();

    }

    private void assertTransposeIsCorrect(int[][] graph, int[][] graphTranspose) {
        int step = 0;
        for (int i = 0; i < graph.length; i++) {
            for (int j = 0; j < graph[i].length; j++) {
                int v = graph[i][j];
                int[] transposeAdjacency = graphTranspose[v];
                boolean edgePresent = false;
                for (int k = 0; k < transposeAdjacency.length; k++) {
                    if(transposeAdjacency[k] == i) edgePresent = true;
                }
                if (!edgePresent)   System.out.println("Hey there is false");
                step++;
            }
        }
        System.out.println(step);
    }
    private void doGraphOperations() {
        long startTime = System.currentTimeMillis();
        int theta = 20000000;
        int rrSets[][] = new int[theta][];
        int totalWidth = 0;
        int totalSize = 0;
        int width = 0;
        int numberOfRRSetsGreaterThanOne = 0;
        for (int i = 0; i < theta; i++) {
            width = 0;
            int random = ThreadLocalRandom.current().nextInt(0, this.n);
            rrSets[i] = new int[0];
            Set<Integer> visited = new HashSet<>();
            Queue<Integer> queue = new LinkedList<>();
            queue.add(random);
            while(!queue.isEmpty()) {
                int vertex = queue.remove();
                if(visited.contains(vertex)) continue;
                visited.add(vertex);
                int[] incomingVertices = this.graphTranspose[vertex];
                for (int incoming:
                     incomingVertices) {
                    width++;
                    if (visited.contains(incoming)) continue;
                    float p = ThreadLocalRandom.current().nextFloat();
                    float propogationProbability = Float.valueOf(1)/Float.valueOf(this.inDegree[vertex]);
                    if(p>propogationProbability) continue;
                    queue.add(incoming);
                }
            }
            int[] rrSet = new int[visited.size()];
            int j =0;
            for(int v: visited) {
                rrSet[j++] = v;
            }
            rrSets[i] = rrSet;
            totalWidth+=width;
            totalSize += rrSets[i].length;
            if(i%1000000==0) {
                System.out.println("Number of RR sets: " + i);
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Time taken " + (endTime - startTime));
        System.out.println("Time taken per RR set" + (endTime - startTime) * 1.0/theta);
        System.out.println("Total width is  " + totalWidth);
        System.out.println("Average width is " + Float.valueOf(totalWidth)/Float.valueOf(theta));
        System.out.println("Total size of RR sets is " + totalSize);
        System.out.println("Average size of RR sets is " + Float.valueOf(totalSize)/Float.valueOf(theta));

    }

    public void writeToFile(String directory, String fileName) {
        File directoryFile = new File(directory);
        if(!directoryFile.exists()) {
            directoryFile.mkdir();
        }
        File output = new File(directory + File.separator + fileName);
        try {
            PrintWriter writer = new PrintWriter(output);

            writer.write(String.format("%d %d\n", this.n, this.m));
            for (int i = 0; i < this.graph.length; i++) {
                int[] edges = this.graph[i];
                for (int v :
                        edges) {
                    writer.write(i + " " + v +"\n");
                }
            }
            writer.flush();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void readGraph(String fileName) {
        HashMap<Integer, Integer> vertexIdMap = new HashMap<>();
        int i =0;
        BufferedReader bufferedReader = null;
        int maxIndegree = 0;
        try {
            InputStream in = new FileInputStream(fileName);
            bufferedReader = new BufferedReader(new InputStreamReader(in));
            String sCurrentLine;
            int m = 0;
            while ((sCurrentLine = bufferedReader.readLine()) != null) {
                String[] inputLine = sCurrentLine.split("\\s", 3);
                int nodeFrom = Integer.parseInt(inputLine[0]);
                int nodeTo = Integer.parseInt(inputLine[1]);


//                Get the ID from the Map
                if(!vertexIdMap.containsKey(nodeFrom)) {
                    vertexIdMap.put(nodeFrom, i);
                    i++;
                }
                if(!vertexIdMap.containsKey(nodeTo)) {
                    vertexIdMap.put(nodeTo, i);
                    i++;
                }

                //Make sure ID is obtained
                nodeFrom = vertexIdMap.get(nodeFrom);
                nodeTo = vertexIdMap.get(nodeTo);

                if (nodeFrom != nodeTo) {
                    addEdge(this.graph, nodeFrom, nodeTo);

                    this.inDegree[nodeTo] = this.inDegree[nodeTo] + 1;
                    if(this.inDegree[nodeTo] > maxIndegree) maxIndegree = this.inDegree[nodeTo];
                    addEdge(this.graphTranspose, nodeTo, nodeFrom);
                }
            }
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Max in degree is " + maxIndegree);
    }

    public void getVerticesAndEdges(String filePath) {
        BufferedReader bufferedReader = null;
        Set<Integer> vertices = new HashSet<>();
        int m = 0;
        try {
            InputStream in = new FileInputStream(filePath);
            bufferedReader = new BufferedReader(new InputStreamReader(in));
            String sCurrentLine;
            while ((sCurrentLine = bufferedReader.readLine()) != null) {
                String[] inputLine = sCurrentLine.split("\\s", 3);
                int nodeFrom = Integer.parseInt(inputLine[0]);
                int nodeTo = Integer.parseInt(inputLine[1]);
                vertices.add(nodeFrom);
                vertices.add(nodeTo);
                m++;
            }
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("*******End of first pass******");
        System.out.println(String.format("Read graph: %s", filePath));
        System.out.println(String.format("n=%d\nm=%d", vertices.size(), m));
        initializeGraphStructure(vertices.size(), m);

    }

    private void initializeGraphStructure(int n, int m) {
        this.graph = new int[n][];
        this.graphTranspose = new int[n][];
        this.inDegree = new int[n];
        this.n = n;
        this.m = m;
    }

    private void addEdge(int[][] graph, int u, int v) {
        int[] neighboursU = graph[u];
        if(neighboursU==null) {
            neighboursU = new int[0];
            graph[u] = neighboursU;
        }
        int[] neighboursV = graph[v];
        if(neighboursV==null) {
            neighboursV = new int[0];
            graph[v] = neighboursV;
        }
        int[] newNeighbours = Arrays.copyOf(neighboursU, neighboursU.length+1);
        newNeighbours[neighboursU.length] = v;
        graph[u] = newNeighbours;

    }
}
