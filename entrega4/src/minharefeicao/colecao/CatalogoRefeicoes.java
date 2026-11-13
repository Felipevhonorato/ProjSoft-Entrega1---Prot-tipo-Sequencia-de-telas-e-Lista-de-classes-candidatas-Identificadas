package minharefeicao.colecao;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import minharefeicao.dominio.PlanoAssinatura;
import minharefeicao.dominio.Refeicao;
import minharefeicao.dominio.PreferenciaAlimentar;
import minharefeicao.dominio.CategoriaRefeicao;

/** Disponibiliza e consulta as opções do catálogo. */
public final class CatalogoRefeicoes {
    private final List<Refeicao> refeicoes = new ArrayList<>();
    public CatalogoRefeicoes() {
        for (var preferencia : PreferenciaAlimentar.values()) {
            for (var categoria : CategoriaRefeicao.values()) {
                var nomes = nomes(preferencia, categoria);
                String prefixo = preferencia.name().substring(0,1) + categoria.name().substring(0,1);
                for (int i = 0; i < nomes.size(); i++)
                    refeicoes.add(new Refeicao(prefixo + (i + 1), nomes.get(i), categoria,
                            Set.of(1, 2, 3), Set.of(preferencia), true));
            }
        }
    }
    private List<String> nomes(PreferenciaAlimentar preferencia, CategoriaRefeicao categoria) {
        return switch (categoria) {
            case PRINCIPAL -> switch (preferencia) {
                case TRADICIONAL -> List.of("Frango grelhado com arroz e feijão", "Carne assada com batatas",
                        "Filé de peixe com legumes", "Almôndegas ao molho de tomate", "Estrogonofe de frango",
                        "Lasanha à bolonhesa", "Picadinho de carne com arroz", "Frango ao curry",
                        "Escondidinho de carne", "Panqueca de frango", "Bife acebolado com arroz",
                        "Tilápia assada com batata-doce", "Arroz de forno com frango", "Frango xadrez",
                        "Macarrão à bolonhesa", "Carne de panela com mandioca", "Omelete com legumes",
                        "Peixe ao molho de tomate", "Frango assado com abóbora", "Risoto de frango", "Lombo assado com arroz");
                case VEGETARIANA -> List.of("Grão-de-bico com arroz integral", "Lasanha de berinjela",
                        "Risoto de cogumelos", "Quibe de abóbora", "Lentilha com legumes", "Panqueca de ricota e espinafre",
                        "Falafel com arroz", "Berinjela recheada com quinoa", "Curry de legumes", "Omelete de espinafre",
                        "Estrogonofe de cogumelos", "Hambúrguer de lentilha com arroz", "Moqueca de banana-da-terra",
                        "Macarrão ao pesto", "Escondidinho de lentilha", "Arroz com brócolis e tofu",
                        "Nhoque ao molho de tomate", "Abobrinha recheada com ricota", "Feijoada vegetariana",
                        "Tofu grelhado com quinoa", "Cuscuz com legumes");
                case SEM_LACTOSE -> List.of("Frango grelhado com arroz integral", "Carne assada com mandioca",
                        "Peixe com legumes no azeite", "Almôndegas com molho de tomate sem leite",
                        "Frango ao molho de leite de coco", "Macarrão de arroz com frango",
                        "Picadinho com abóbora", "Frango ao curry com leite de coco", "Carne de panela com batatas",
                        "Arroz de frango com cenoura", "Bife acebolado com feijão", "Tilápia com arroz integral",
                        "Arroz de forno sem queijo", "Frango xadrez sem leite", "Espaguete ao alho e azeite",
                        "Lombo com mandioca", "Omelete sem leite com legumes", "Peixe ao molho de coco",
                        "Frango assado com batata-doce", "Quinoa com frango e legumes", "Carne desfiada com arroz");
            };
            case ACOMPANHAMENTO -> switch (preferencia) {
                case TRADICIONAL -> List.of("Arroz branco", "Feijão-carioca", "Purê de batata", "Legumes no vapor", "Farofa de cenoura");
                case VEGETARIANA -> List.of("Arroz integral", "Feijão-preto sem carne", "Abóbora assada", "Brócolis no vapor", "Salada de quinoa");
                case SEM_LACTOSE -> List.of("Arroz com cenoura", "Feijão-carioca sem leite", "Batata assada no azeite", "Legumes no vapor", "Farofa no azeite");
            };
            case SOBREMESA -> switch (preferencia) {
                case TRADICIONAL -> List.of("Pudim de leite", "Mousse de chocolate", "Salada de frutas");
                case VEGETARIANA -> List.of("Arroz-doce com canela", "Mousse de maracujá", "Banana assada com canela");
                case SEM_LACTOSE -> List.of("Mousse de cacau com leite de coco", "Salada de frutas", "Doce de abóbora com coco");
            };
        };
    }
    public List<Refeicao> filtrar(PlanoAssinatura plano, Set<PreferenciaAlimentar> preferencias, CategoriaRefeicao categoria) {
        return refeicoes.stream().filter(r -> r.compativel(plano, preferencias, categoria)).toList();
    }
    public Refeicao obter(String id) {
        return refeicoes.stream().filter(r -> r.id().equalsIgnoreCase(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Refeição não encontrada."));
    }
}
