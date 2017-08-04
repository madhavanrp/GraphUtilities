package com.graphutilities;

import sun.security.provider.certpath.Vertex;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class GraphPreprocessor {

    private Object[] graph;
    private Object[] graphTranspose;

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

//        writeToFile(outputPath, fileName);
        doGraphOperations();

    }

    private void doGraphOperations() {
        System.out.println("Starting rr");
        long startTime = System.currentTimeMillis();
        int theta = 1200000000;
        int rrSets[][] = new int[theta][];
        for (int i = 0; i < theta; i++) {

            int random = ThreadLocalRandom.current().nextInt(0, this.n);
            List<Integer> outbound =  (List<Integer>)this.graph[random];
            rrSets[i] = new int[0];
            Set<Integer> visited = new HashSet<>();
            Queue<Integer> queue = new LinkedList<>();
            queue.add(random);
            int width = 0;
            while(!queue.isEmpty()) {
                List<Integer> incomingVertices = (List<Integer>)this.graphTranspose[queue.remove()];
                for (int incoming:
                     incomingVertices) {
                    if (visited.contains(incoming)) continue;
                    float p = ThreadLocalRandom.current().nextFloat();
                    if(p>0.01f) continue;
                    queue.add(incoming);
                    width++;
                    int[] existingRRSet = rrSets[i];
                    int[] newRRSet = Arrays.copyOf(existingRRSet, existingRRSet.length+1);
                    newRRSet[existingRRSet.length] = incoming;
                }
            }
//            int j =0;
//            for (Integer v :
//                    outbound) {
//                rrSets[i][j++] = v;
//            }
            if(i%1000000==0) {
                System.out.println("Number of RR sets: " + i);
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Time taken " + (endTime - startTime));

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
                List<Integer> edges = (List<Integer>)this.graph[i];
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
        try {
            InputStream in = new FileInputStream(fileName);
            bufferedReader = new BufferedReader(new InputStreamReader(in));
            String sCurrentLine;
            int m = 0;
            while ((sCurrentLine = bufferedReader.readLine()) != null) {
                String[] inputLine = sCurrentLine.split("\\s", 3);
                int nodeFrom = Integer.parseInt(inputLine[0]);
                int nodeTo = Integer.parseInt(inputLine[1]);


                //Get the ID fromm the Map
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

                    addEdge(this.graphTranspose, nodeTo, nodeFrom);
                }
            }
            bufferedReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        this.graph = new Object[n];
        this.graphTranspose = new Object[n];
        this.inDegree = new int[n];
        this.n = n;
        this.m = m;
    }

    private void addEdge(Object[] graph, int u, int v) {
        List<Integer> neighboursU = (List<Integer>)graph[u];
        if(neighboursU==null) {
            neighboursU = new ArrayList<>();
            graph[u] = neighboursU;
        }
        List<Integer> neighboursV = (List<Integer>)graph[v];
        if(neighboursV==null) {
            neighboursV = new ArrayList<>();
            graph[v] = neighboursV;
        }
        neighboursU.add(v);

    }
}
