# Roteiro do vídeo

O vídeo usa os diagramas UML da entrega N2, trechos do código e saídas reais dos testes que exercitam a interface de terminal. As legendas em português explicam o que acontece em cada trecho.

1. Apresente a contratação e as três camadas: TelaAssinatura, ControladorAssinatura e domínio.
2. Mostre as mensagens 01.01 a 01.08 no diagrama de sequência e sua correspondência no código.
3. Percorra plano, preferências, seleção, resumo, endereço e pagamento autorizado. Mostre assinatura ativa, pedido aprovado, previsão e protocolo.
4. Fluxo alternativo 1: três erros no código, reenvio e validação do novo código.
5. Fluxo alternativo 2: tentativa de selecionar o sétimo prato, rejeição sem mutação e ajuste da seleção.
6. Fluxo alternativo 3: recusa, nova tentativa com o mesmo identificador, autorização com outro e desistência em uma sessão separada.
7. Feche com a releitura dos arquivos TXT, comparando as tentativas registradas e o estado final.

Para repetir as execuções do vídeo, use `executar.ps1 -Testar`. O terminal e os arquivos persistidos são os mesmos usados pela aplicação interativa.
