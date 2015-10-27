import java.awt.EventQueue;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JList;
import javax.swing.JPanel;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Image;

import javax.swing.JProgressBar;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


public class MainWindow implements GraphLoadingListener, GraphPanelBuilderListener{

    private JFrame frame;
    /**
     * @wbp.nonvisual location=39,487
     */
    private JProgressBar progressBar = new JProgressBar();
    private JList<String> mList;
    private JList<String> mListCountries;
    private JPanel mGraphAndMapPanel = new JPanel(); // panel na którym rysowany jest graf i mapa
    private DefaultListModel<String> mListModel = new DefaultListModel<String>();
    private DefaultListModel<String> mListCountriesModel = new DefaultListModel<String>();

    final private String fNoCountrySelection = "none"; //wybór "żadnego kraju"

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    MainWindow window = new MainWindow();
                    window.frame.setVisible(true);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public MainWindow() {
        mList = new JList<String>(mListModel);
        mListCountries = new JList<String>(mListCountriesModel);
        mListCountries.addMouseListener(new MouseAdapter(){
            @Override
            public void mousePressed(MouseEvent e) {
                JList<String> tmp_list = (JList<String>)e.getSource();
                int index = tmp_list.locationToIndex(e.getPoint());
                try{
                    String name_of_country = mListCountriesModel.get(index);

                    //ladujemy nazwy firm
                    Map<String,CompanyServices> companies = DataParser.getCompaniesData();

                    Set<String> company_names;
                    if(!name_of_country.equals(fNoCountrySelection)){
                        company_names = new TreeSet<String>();
                        Object[] c_n_tmp = companies.keySet().toArray();
                        for(int i = 0; i < c_n_tmp.length; ++i){
                            Map<String, ArrayList<CompanyServices.Triple<String,String,String>>> t_o_s_tmp = 
                                    companies.get(c_n_tmp[i]).getAllServices();
                            Object[] triple_keys_tmp = t_o_s_tmp.keySet().toArray();
                            for(int j =0; j < t_o_s_tmp.size(); ++j){
                                boolean found = false;
                                for(CompanyServices.Triple<String,String,String> tmp_triple: t_o_s_tmp.get(triple_keys_tmp[j])){
                                    if(tmp_triple.third.equals(name_of_country)){
                                        company_names.add((String)c_n_tmp[i]);
                                        found = true;
                                        break;
                                    }
                                }

                                if(found){
                                    break;
                                }
                            }
                        }
                    }else{
                        company_names = new TreeSet<String>(companies.keySet());
                    }

                    mListModel.clear();
                    for(String nm: company_names){
                        mListModel.addElement(nm);
                    }

                }catch(ArrayIndexOutOfBoundsException exception){
                    System.out.println("Index is bad :" + String.valueOf(index));
                }
            }
        });

        mList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                JList<String> tmp_list = (JList<String>)e.getSource();
                int index = tmp_list.locationToIndex(e.getPoint());
                try{
                    String name_of_company = mListModel.get(index);
                    GraphPanelBuilder.createGraphAndMap(name_of_company,mGraphAndMapPanel.getWidth(),
                            (int)(mGraphAndMapPanel.getHeight() * 0.6));
                }catch(ArrayIndexOutOfBoundsException exception){
                    System.out.println("Index is bad :" + String.valueOf(index));
                }
            }
        });
        DataParser.setLoadingListener(this);
        GraphPanelBuilder.setListener(this);
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame();
        frame.setBounds(50, 50, 1400, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);

        JMenu mnFile = new JMenu("File");
        menuBar.add(mnFile);

        JMenuItem mntmOpen = new JMenuItem("Open");
        mntmOpen.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                
                JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
                FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    "CSV files", "csv");
                chooser.setFileFilter(filter);
                int returnVal = chooser.showOpenDialog(frame);
                if(returnVal == JFileChooser.APPROVE_OPTION) {
                    DataParser.loadGraph(chooser.getSelectedFile().getAbsolutePath());//.getName());
                }
            }
        });
        mnFile.add(mntmOpen);

        JMenuItem mntmClose = new JMenuItem("Close");
        mnFile.add(mntmClose);
        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.rowWeights = new double[]{0.2,0.97,0.03}; // inaczej się zmniejsza w pionie, bez sensu
        gridBagLayout.columnWeights = new double[]{0.0, 0.85}; //pierwszy musi być zero by można było wymuscieć później ustaloną wielkość
        frame.getContentPane().setLayout(gridBagLayout);

        GridBagConstraints gbc_list_c = new GridBagConstraints();
        gbc_list_c.fill = GridBagConstraints.BOTH;
        gbc_list_c.gridx = 0;
        gbc_list_c.gridy = 0;
        //gbc_list.weighty = 1.0;
        //gbc_list.gridheight = 9;
        JScrollPane scroll2 = new JScrollPane(mListCountries);

        scroll2.setMinimumSize(new Dimension(250, Integer.MIN_VALUE));
        //scroll.setPreferredSize(new Dimension(200, Integer.MIN_VALUE));
        scroll2.setMaximumSize(new Dimension(250, Integer.MAX_VALUE));

        frame.getContentPane().add(scroll2, gbc_list_c);

        GridBagConstraints gbc_list = new GridBagConstraints();
        gbc_list.fill = GridBagConstraints.BOTH;
        gbc_list.gridx = 0;
        gbc_list.gridy = 1;
        //gbc_list.weighty = 1.0;
        //gbc_list.gridheight = 9;
        JScrollPane scroll = new JScrollPane(mList);

        scroll.setMinimumSize(new Dimension(250, Integer.MIN_VALUE));
        //scroll.setPreferredSize(new Dimension(200, Integer.MIN_VALUE));
        scroll.setMaximumSize(new Dimension(250, Integer.MAX_VALUE));

        frame.getContentPane().add(scroll, gbc_list);

        GridBagConstraints gbc_progressbar = new GridBagConstraints();
        gbc_progressbar.fill = GridBagConstraints.BOTH;
        gbc_progressbar.gridx = 0;
        gbc_progressbar.gridy = 2;
        //gbc_progressbar.gridy = 9;
        //gbc_progressbar.gridheight = 1;
        //progressBar.setPreferredSize(new Dimension(200, 50));
        frame.getContentPane().add(progressBar, gbc_progressbar);

        GridBagConstraints gbc_graphAndMapPanel = new GridBagConstraints();
        gbc_graphAndMapPanel.fill = GridBagConstraints.BOTH;
        gbc_graphAndMapPanel.gridx = 1;
        gbc_graphAndMapPanel.gridy = 0;
        gbc_graphAndMapPanel.gridheight = 3;
        frame.getContentPane().add(mGraphAndMapPanel, gbc_graphAndMapPanel);
    }

    public void setLevelReady(int percents) {
        progressBar.setValue(percents);
    }

    /* (non-Javadoc)
     * Funkcja wczytująca dane do okna z nazwami firm
     * @see GraphLoadingListener#loadingReady()
     */
    public void loadingReady() {

        SwingUtilities.invokeLater(new Runnable(){
            public void run() {
                setLevelReady(100);
                Map<String,ArrayList<String>> country_names = DataParser.getCountriesAndCitiesData();
                Set<String> ordered_country_names = new TreeSet<String>(country_names.keySet());
                mListCountriesModel.clear();

                mListCountriesModel.addElement(fNoCountrySelection);
                for(String country_n: ordered_country_names){
                    mListCountriesModel.addElement(country_n);
                }

                //ladujemy nazwy firm
                Map<String,CompanyServices> companies = DataParser.getCompaniesData();
                Set<String> company_names = new TreeSet<String>(companies.keySet());
                mListModel.clear();
                for(String nm: company_names){
                    mListModel.addElement(nm);
                }
            }  
        });
    }

    private Image sMapImage; // aby nie uzywać final
    public void workIsDone() {

        SwingUtilities.invokeLater(new Runnable(){
            public void run() {
                mGraphAndMapPanel.removeAll();
                GridBagLayout gridBagLayout = new GridBagLayout();
                gridBagLayout.rowWeights = new double[]{0.6,0.4}; // inaczej się zmniejsza w pionie, bez sensu
                gridBagLayout.columnWeights = new double[]{0.85}; //większe od zera by się rozpychało
                mGraphAndMapPanel.setLayout(gridBagLayout);

                GridBagConstraints gbc_panel = new GridBagConstraints();
                gbc_panel.fill = GridBagConstraints.BOTH;
                gbc_panel.gridx = 0;
                gbc_panel.gridy = 1;
                Component graph_panel = GraphPanelBuilder.getGraphPanel();
                JScrollPane scroll = new JScrollPane(graph_panel);
                mGraphAndMapPanel.add(scroll, gbc_panel);

                GridBagConstraints gbc_map_panel = new GridBagConstraints();
                gbc_map_panel.fill = GridBagConstraints.BOTH;
                gbc_map_panel.gridx = 0;
                gbc_map_panel.gridy = 0;

                byte[] map_byte_data = GraphPanelBuilder.getMap();
                JLabel map_panel;
                if(map_byte_data != null){
                    sMapImage = frame.getToolkit().createImage(GraphPanelBuilder.getMap());
                    map_panel = new JLabel(new ImageIcon(sMapImage));
                }else{
                    map_panel = new JLabel("Nie można pobrać Mapy");
                }

                JScrollPane scroll2 = new JScrollPane(map_panel);
                mGraphAndMapPanel.add(scroll2, gbc_map_panel);
                frame.getContentPane().revalidate();
                frame.getContentPane().repaint();
            }  
        });
    }

}
