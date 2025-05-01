import javax.swing.*;
import java.awt.*;
import java.time.*;
import java.time.format.*;
import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.util.List;

public class CalculaFaltas extends JFrame {
    private JComboBox<Integer> cmbDiaInicio, cmbMesInicio, cmbAnoInicio;
    private JComboBox<Integer> cmbDiaFim, cmbMesFim, cmbAnoFim;
    private JComboBox<Double> cmbPercentual;
    private JCheckBox chkDoisHorarios;
    private JCheckBox[] checkboxesDias;
    private JTextArea txtResultado;
    private JSpinner spnFaltasAtuais;

    public CalculaFaltas() {
        initComponents();
    }
    private JPanel createCenteredPanel(Component comp) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.add(comp);
        return panel;
    }
    private void initComponents() {
        setTitle("Calculadora de Faltas da Unifor ADS");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 700);
        setLayout(new BorderLayout(10, 10));
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Componentes
        cmbDiaInicio = new JComboBox<>();
        cmbMesInicio = new JComboBox<>();
        cmbAnoInicio = new JComboBox<>();
        JPanel panelDataInicio = createDatePanel(cmbDiaInicio, cmbMesInicio, cmbAnoInicio);
        cmbDiaFim = new JComboBox<>();
        cmbMesFim = new JComboBox<>();
        cmbAnoFim = new JComboBox<>();
        JPanel panelDataFim = createDatePanel(cmbDiaFim, cmbMesFim, cmbAnoFim);
        cmbPercentual = new JComboBox<>(new Double[]{75.0, 80.0});
        chkDoisHorarios = new JCheckBox("Aula dupla no mesmo dia?");
        checkboxesDias = new JCheckBox[5];
        JButton btnCalcular = new JButton("Calcular");
        txtResultado = new JTextArea(8, 30);
        mainPanel.add(createCenteredPanel(new JLabel("Faltas Atuais:")));
        spnFaltasAtuais = new JSpinner(new SpinnerNumberModel(0, 0, 999, 1));
        mainPanel.add(createCenteredPanel(spnFaltasAtuais));

        // Configuração dos dias da semana
        String[] dias = {"Segunda", "Terça", "Quarta", "Quinta", "Sexta"};
        JPanel panelDias = new JPanel(new GridLayout(1, 5));
        for(int i = 0; i < 5; i++) {
            checkboxesDias[i] = new JCheckBox(dias[i]);
            panelDias.add(checkboxesDias[i]);
        }

        // Layout
        mainPanel.add(createCenteredPanel(new JLabel("Data Início:")));
        mainPanel.add(createCenteredPanel(panelDataInicio));
        mainPanel.add(createCenteredPanel(new JLabel("Data Fim:")));
        mainPanel.add(createCenteredPanel(panelDataFim));
        mainPanel.add(createCenteredPanel(new JLabel("Percentual Requerido (%):")));
        mainPanel.add(createCenteredPanel(cmbPercentual));
        mainPanel.add(createCenteredPanel(chkDoisHorarios));
        mainPanel.add(createCenteredPanel(new JLabel("Dias com Aula:")));
        mainPanel.add(createCenteredPanel(panelDias));
        mainPanel.add(createCenteredPanel(btnCalcular));
        JScrollPane scrollResultado = new JScrollPane(txtResultado);
        scrollResultado.setPreferredSize(new Dimension(500, 100));  // Aumentar altura
        mainPanel.add(createCenteredPanel(scrollResultado));

        // Ação do botão
        btnCalcular.addActionListener(e -> calcularFaltas());
        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createDatePanel(JComboBox<Integer> dia, JComboBox<Integer> mes, JComboBox<Integer> ano) {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

    // Popular comboboxes
    for(int i = 1; i <= 31; i++) dia.addItem(i);
    for(int i = 1; i <= 12; i++) mes.addItem(i);
    for(int i = Year.now().getValue() - 5; i <= Year.now().getValue() + 5; i++) ano.addItem(i);

    // Configurar tamanhos
    dia.setPreferredSize(new Dimension(60, 25));
    mes.setPreferredSize(new Dimension(80, 25));
    ano.setPreferredSize(new Dimension(80, 25));

    panel.add(new JLabel("Dia:"));
    panel.add(dia);
    panel.add(new JLabel("Mês:"));
    panel.add(mes);
    panel.add(new JLabel("Ano:"));
    panel.add(ano);
    LocalDate hoje = LocalDate.now();
    dia.setSelectedItem(hoje.getDayOfMonth());
    mes.setSelectedItem(hoje.getMonthValue());
    ano.setSelectedItem(hoje.getYear());

    return panel;
}

    private void calcularFaltas() {
    try {
        // Obter valores numéricos corretamente
        int diaInicio = (Integer) cmbDiaInicio.getSelectedItem();
        int mesInicio = (Integer) cmbMesInicio.getSelectedItem();
        int anoInicio = (Integer) cmbAnoInicio.getSelectedItem();

        int diaFim = (Integer) cmbDiaFim.getSelectedItem();
        int mesFim = (Integer) cmbMesFim.getSelectedItem();
        int anoFim = (Integer) cmbAnoFim.getSelectedItem();

        LocalDate inicio = LocalDate.of(anoInicio, mesInicio, diaInicio);
        LocalDate fim = LocalDate.of(anoFim, mesFim, diaFim);

            // Validar datas
            if(fim.isBefore(inicio)) {
                JOptionPane.showMessageDialog(this, "Data final deve ser após data inicial!");
                return;
            }

            // Obter feriados
            Set<LocalDate> feriados = carregarFeriados();

            // Dias selecionados (2-6 → 1-5 em DayOfWeek)
            Set<DayOfWeek> diasAula = new HashSet<>();
            for(int i = 0; i < 5; i++) {
                if(checkboxesDias[i].isSelected()) {
                    diasAula.add(DayOfWeek.of(i + 2));
                }
            }

            // Calcular dias de aula válidos
            long totalDiasAula = 0;
            LocalDate data = inicio;
            while(!data.isAfter(fim)) {
                if(!feriados.contains(data) && diasAula.contains(data.getDayOfWeek())) {
                    totalDiasAula++;
                }
                data = data.plusDays(1);
            }

            // Ajustar para aulas duplas
            if(chkDoisHorarios.isSelected()) totalDiasAula *= 2;

            // Calcular faltas permitidas
            double percentual = (Double) cmbPercentual.getSelectedItem();
            int faltasPermitidas = (int) Math.floor(totalDiasAula * (100 - percentual) / 100);

             int faltasAtuais = (Integer) spnFaltasAtuais.getValue();
            int faltasRestantes = Math.max(faltasPermitidas - faltasAtuais, 0);

            String resultado = String.format(
                "Total de aulas: %d\nFaltas permitidas: %d\nFaltas atuais: %d\nFaltas restantes: %d",
                totalDiasAula,
                faltasPermitidas,
                faltasAtuais,
                faltasRestantes
            );
            txtResultado.setText(resultado);
        } catch(DateTimeException e) {
        JOptionPane.showMessageDialog(this, "Data inválida!\nVerifique se o dia existe para o mês selecionado");
    }

    }

    private Set<LocalDate> carregarFeriados() {
    Set<LocalDate> feriados = new HashSet<>();
    try {
        Path path = Paths.get(".\\feriados.txt");
        System.out.println("Caminho do arquivo: " + path.toAbsolutePath()); // Log do caminho

        if (!Files.exists(path)) {
            System.out.println("Arquivo não encontrado. Criando novo...");
            Files.createFile(path);
        }

        List<String> linhas = Files.readAllLines(path);
        System.out.println("Linhas lidas: " + linhas.size()); // Log da quantidade de linhas

        for (String linha : linhas) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate data = LocalDate.parse(linha.trim(), formatter);
                feriados.add(data);
            } catch (DateTimeParseException e) {
                System.err.println("Linha inválida: " + linha); // Log de linha inválida
            }
        }
    } catch (IOException e) {
        JOptionPane.showMessageDialog(this, "Erro ao ler arquivo de feriados: " + e.getMessage());
        e.printStackTrace(); // Log completo do erro
    }
    return feriados;
}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new CalculaFaltas().setVisible(true);
        });
    }
}