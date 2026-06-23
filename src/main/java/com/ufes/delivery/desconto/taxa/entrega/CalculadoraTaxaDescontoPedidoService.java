package com.ufes.delivery.desconto.taxa.entrega;

import com.mycompany.logsauditoria.LogEntry;
import com.mycompany.logsauditoria.interfaces.ILogger;
import com.ufes.delivery.model.CupomDescontoEntrega;
import com.ufes.delivery.model.Pedido;
import com.ufes.delivery.util.UsuarioLogadoService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculadoraTaxaDescontoPedidoService {
    private List<IFormaDescontoTaxaEntrega> metodosDeDesconto;
    private ILogger logger;

    public CalculadoraTaxaDescontoPedidoService(ILogger logger) {
        metodosDeDesconto = new ArrayList<>();
        this.logger = logger;

        metodosDeDesconto.add(new FormaDescontoTaxaPorBairro());
        metodosDeDesconto.add(new FormaDescontoTaxaPorTipoCliente());
        metodosDeDesconto.add(new FormaDescontoTipoItem());
        metodosDeDesconto.add(new FormaDescontoValorPedido());
    }

    public void calcularDesconto(Pedido pedido) {
        pedido.limparCuponsDescontoEntrega();

        double limiteAplicavel = pedido.getTaxaEntrega();

        for (IFormaDescontoTaxaEntrega formaDescontoTaxaEntrega : metodosDeDesconto) {
            double totalDescontos = pedido.getTotalDescontosTaxaEntrega();

            if (formaDescontoTaxaEntrega.seAplica(pedido) && totalDescontos < limiteAplicavel) {
                CupomDescontoEntrega cupom = formaDescontoTaxaEntrega.calcularDesconto(pedido);
                double limiteRestante = limiteAplicavel - totalDescontos;
                double valorAplicado = Math.min(cupom.getValorDesconto(), limiteRestante);
                cupom.aplicar(valorAplicado);

                if (cupom.getValorDesconto() > 0) {
                    pedido.adicionarCupomDescontoEntrega(cupom);
                }
            }
        }
        
        Map<String, String> dadosExtra = new HashMap<>();
        dadosExtra.put("codigo_pedido", pedido.getCodigo()); // Aquele atributo que você criou na classe Pedido
        dadosExtra.put("nome_cliente", pedido.getCliente().getNome());

        try {
            LogEntry log = new LogEntry(
                UsuarioLogadoService.getNomeUsuario(), // Pega o usuário aleatório
                LocalDate.now(),
                LocalTime.now(),
                "Aplicação de cupom de desconto nas taxas de entrega", // Nome da operação exigido no PDF
                dadosExtra
            );
            
            // Chama o método da interface (pode ser registrar() ou log(), dependendo de como a equipe de log nomeou)
            logger.registrar(log); 
            
        } catch (Exception e) {
            // Evita que um erro na hora de salvar o arquivo de log derrube o sistema inteiro
            System.out.println("Falha ao registrar log da taxa de entrega: " + e.getMessage());
        }
        
    }
}
