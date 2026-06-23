package com.ufes.delivery;

import com.ufes.delivery.desconto.pedido.AplicadorCupomPedidoService;
import com.ufes.delivery.desconto.taxa.entrega.CalculadoraTaxaDescontoPedidoService;
import com.ufes.delivery.model.Cliente;
import com.ufes.delivery.model.CupomDescontoPedido;
import com.ufes.delivery.model.Item;
import com.ufes.delivery.model.Pedido;
import com.ufes.delivery.repository.CupomRepositoryEmMemoria;

import com.mycompany.logsauditoria.interfaces.ILogger;
import com.mycompany.logsauditoria.JsonLogger;
import com.mycompany.logsauditoria.LogEntry;
import com.ufes.delivery.util.UsuarioLogadoService;
import java.io.IOException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class UmCasoDeUsoDePedido {

    public static void main(String[] args) {
        
        ILogger logger = new JsonLogger("delivery_auditoria.json");

        Cliente cliente = new Cliente("Maria", "Ouro", 1, "Limoeiro", "Cidade Maravilhosa", "Castelo");
        LocalDateTime dataPedido = LocalDateTime.now();
        
        Pedido pedido = new Pedido(dataPedido, cliente, "PD-01");
        

        Item item1 = new Item("Caderno", 2, 10.50, "Educacao");
        Item item2 = new Item("Borracha", 5, 4.25, "Educacao");
        pedido.adicionarItem(item1);
        pedido.adicionarItem(item2);

        // 3. Injeta o logger no serviço de taxa de entrega
        CalculadoraTaxaDescontoPedidoService calculadoraDeDesconto = new CalculadoraTaxaDescontoPedidoService(logger);
        calculadoraDeDesconto.calcularDesconto(pedido);

        CupomRepositoryEmMemoria cupomRepository = new CupomRepositoryEmMemoria();
        cupomRepository.adicionarCupom(
                new CupomDescontoPedido("VALIDOHOJE", 15.0, dataPedido.minusDays(1), dataPedido.plusDays(1)));

        // 4. Injeta o logger no serviço de desconto do pedido
        AplicadorCupomPedidoService aplicadorCupomService = new AplicadorCupomPedidoService(cupomRepository, logger);

        LocalDateTime dataHoraAplicacaoCupom = LocalDateTime.now();
        aplicadorCupomService.aplicarCupom(pedido, "VALIDOHOJE", dataHoraAplicacaoCupom);

        // 5. Exemplo de log na falha operacional (Exigência do documento CR2)
        try {
            aplicadorCupomService.aplicarCupom(pedido, "CUPOMINEXISTENTE", dataHoraAplicacaoCupom);
        } catch (RuntimeException ex) {
            System.out.println(ex.getMessage());
            
            // Cria o log específico para a exceção
            Map<String, String> dadosFalha = new HashMap<>();
            dadosFalha.put("codigo_pedido", pedido.getCodigo());
            dadosFalha.put("motivo_falha", ex.getMessage());

            try {
                logger.registrar(new LogEntry(
                    UsuarioLogadoService.getNomeUsuario(),
                    LocalDate.now(),
                    LocalTime.now(),
                    "Falha ao aplicar cupom (Exceção)",
                    dadosFalha
                ));
            } catch (IOException logEx) {
                System.out.println("Não foi possível gravar o log da falha.");
            }
        }

        // 6. Chama o cálculo total usando a versão do método que recebe o logger
        pedido.calcularValorTotal(logger);

        System.out.println(pedido);
    }
}