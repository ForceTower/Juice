# Juice, a Sagres Scrapper Library
[![Release](https://jitpack.io/v/ForceTower/Juice.svg)](https://jitpack.io/#ForceTower/Juice)

Desde o inicio do UNES, sempre foi necessário criar um pacote que envolvesse as requisições ao portal e que exposse os itens encontrados lá de forma de uma API, ou seja, com funções, retornos e tipos bem definidos.

Este repositório contem o que é utilizado pelo UNES para extrair as informações do portal. Se o erro é de "backend" muito provavelmente este é o lugar que está errado, e não o [Melon](https://github.com/ForceTower/Melon).

Este repositório está disponível como uma dependência no jitpack para fácil acesso :)

## Metas
* Suporte a suspend functions to kotlin, já que é uma feature bem interessante da linguagem :)
* Codigo completamente revisado para kotlin
* Código livre da Framework Android
* Testes automáticos, codigo testável é legal
* Suporte descente para o Mestrado

Talvez
* Codigo com suporte ao Android 4.1~4.4? (Significa que precisa de um downgrade no OkHttp)

## História
### Arquitetura 1 (0.0.0a ~ 2.1.4b)
No projeto arquitetura 1 do UNES isso foi atingido com a criação do [Sagres SDK](https://github.com/ForceTower/Pineapple/tree/2.1.4b/app/src/main/java/com/forcetower/uefs/sagres_sdk), uma ideia simples e facil de implementar. Ela continha os parsers e uma maneira para salvar todos os dados que eram obtidos através da API. 

Contudo, a API falhava de maneira catastrofica em casos de concorrencia. Como tudo ficava salvo em um arquivo JSON centralizado uma escrita errada neste arquivo acarretava em uma série de transformações e possiveis notificações para o usuário, como o bug antigo das notificações de mensagens aparecendo o tempo todo.

Toda a API era baseada em callbacks, o que fazia as coisas se tornarem bem interessantes e similares ao callback hell de Javascript.
Essa arquitetura era interessante pois tudo, ou quase tudo, que fazia requisições ao portal estava apenas em 1 lugar, era uma API centralizada, elas usavam, ou pelo menos tentavam, usar apenas as suas coisas e o aplicativo inteiro dependia disso.

Aplicação em uma frase:
Pediu para fazer a atualização das mensagens? Bem me passe esse callback para eu te avisar que as coisas foram modificadas e te enviar os dados modificados assim que acabar. Perdeu a referência do callback? Realmente uma pena, agora se quiser um aviso que as mensagens chegaram me chame de novo e irei criar uma nova requisição e não cancelarei a anterior :)

### Arquitetura 2 (3.0.0 ~ 6.8.4)
Aqui as coisas meio que se perderam, a ideia era simples, corrigir bugs e tornar a API mais reativa para que eu não recebesse mensagens do tipo: "Eu recebi a notificação mas não vi a mensagem na lista", e eu ter que responder: "Vc precisa abrir e fechar o aplicativo ou mudar de tela :v"

No final das contas, não se sabe exatamente quem chamar para receber a informação desejada, mas quando você lembrava onde estava a coisa, ficava até que legal. A API basicamente estava dividida nos pacotes [Repository](https://github.com/ForceTower/Pineapple/tree/_new_/app/src/main/java/com/forcetower/uefs/rep), que possuia coisas relacionadas e não relacionadas à API e o pacote [Sagres](https://github.com/ForceTower/Pineapple/tree/_new_/app/src/main/java/com/forcetower/uefs/sgrs) que tinha os parsers, os models estavam junto com o banco de dados. Enfim, fez sentido no passado (mentira, não fez tanto assim).

Coisas que deram certo aqui: Bugs no parser diminuiram bastante e a API se tornou reativa graças ao LiveData + Room. Para começar o arquivo JSON que salvava os dados foi substituido por uma solução mais robusta o Room Database (que era novo e brilhante na epoca)

Coisas que deram errado aqui? Bem, a reestruturação da API junto com meu hype de reatividade deixaram as coisas engraçadas. A API não estava mais em um lugar único e bem definido, na verdade ela foi separada em lugares que fazem requisição, lugares que fazem parse da requisição e lugares que processam e salvam estes dados.

No final das contas não era possivel saber exatamente onde a requisição era feita, e muito possívelmente existia código duplicado em alguns lugares já que a API não era mais concisa.

Aplicação em uma frase:
Quer atualizar todos os dados? Muito bem, chame este método nesta classe AlgoRepository e aguarde as mudanças, tudo será mostrado a você automaticamente a medida que os dados forem atualizando, não precisa se preocupar, observe este dado aqui que ele te informará se ja terminamos. Enquanto isso vou passar por vários transformadores de chamada em LiveData, e fazer uma coisa linda com MediatorLiveData's para transformar essa sua unica chamada em algo super complicado de transformações de LiveData. Opa, enquanto falavamos, eu removi 5 fontes e adicionei mais 2.
Você deseja chamar isso tudo de forma sincrona? Realmente uma pena não é, mas isso não é trivial pois tudo está preparado para ser assincrono.

### Arquitetura 3 (7.0.0 ~ X.X.X)
Com a chegada do Kotlin como uma linguaguem first class para Android e das mudanças drasticas que o sistema passaria no "futuro" eu decidi sair da zona de conforto, apesar de algumas coisas ainda estarem escritas em Java, e começar a reescrever tudo nessa linguagem interessante e remover todas as coisas chatas que da API anterior

Então, desta vez decidi implementar o módulo do Sagres como algo totalmente separado do modulo do aplicativo, então o projeto Melon (ou UNES 2 [Na verdade é a terceira reestruturação, então é UNES 3 {Chega de parenteses}]) nasceu com um módulo [Sagres](https://github.com/ForceTower/Melon/tree/7.8.3/sagres/src/main/java/com/forcetower/sagres).

Metas simples:
* Separação completa da API do aplicativo.
* Chamadas sincronas e assincronas para cada método.

Então, percebi que a separação de cada tarefa basica feita no portal tambem era algo muito interessante, ou seja, para o login na plataforma, existiria uma classe LoginOperation que é especializada em fazer login e resolver problemas de aprovação que podem existir.
Buscar as mensagens? Existe a MessagesOperation que simplesmente vai até o lugar que tem mensagens e me devolve elas. A partir dai, decidi que os models dessa API tambem deveriam ser restritos apenas a ela, para que pudessem ser reutilizados.

Nasceu assim o [SagresNavigator](https://github.com/ForceTower/Melon/blob/7.8.3/sagres/src/main/java/com/forcetower/sagres/SagresNavigator.kt), uma classe abstrata que possui todos os métodos para todas as tarefas automatizadas implementadas. Com métodos sincronos (method) e metodos assincronos (os iniciados com aMethod).

A construção dessa nova API deixou o código bem mais enxuto, fácil de manipular e manter já que tudo está separado em seu próprio lugar. Os bugs para a graduação e na UEFS quase não existem mais, pelo menos não há mais nenhum conhecido :)

Coisas legais dessa API:
* Com uma simples chamada de initialize(context: Context) você terá uma instancia para todas as funções disponíveis do portal.
* É possivel trocar o endpoint da instituição de ensino apenas adicionando os atributos nas [Constants](https://github.com/ForceTower/Melon/blob/7.8.3/sagres/src/main/java/com/forcetower/sagres/Constants.kt)
* Caso algo pare de funcionar para uma tarefa especifica basta editar a Operation a qual ela se refere e retornar os dados necessários ;)
* Uma tarefa pode se comunicar com outra basta chamar o método da classe abstrata, o que evita reescrita de código

Coisas não legais dessa API:
* O código é totalmente dependente da framework android
* Não é 100% Kotlin

Ou seja, quero testar esse codigo sem estar com meu celular? Não pode, precisa de um context para que tudo faça sua mágica. 
Quero usar isso em um projeto aleatório? Não tem como, na verdade, copie tudo e remova as coisas android e substitua por chamadas equivalentes :)
O código não é 100% kotlin, então nada de 100% null-safety, apesar de erros como este serem muito raros :v
É uma android library, então transformar isso em um paccote de hotswap significa que é preciso utilizar reflection em uma camada para tornar tudo "acessível"

### Arquitetura 4 (X.X.X ~ X.X.X) - (7.9.0 ~ X.X.X)u
Essa API vem para resolver os 2 pontos não tão legais da arquitetura 3, nosa, mas precisa mesmo de uma arquitetura diferente? Bem... Pode ser argumentável das duas formas, o código não mudou fundamentalmente, mas ele tambem adiciona mudanças que quebram a compatibilidade com a API anterior então, arquitetura 4 é ela.

## Disclaimer
Este serviço não é licenciado nem tem qualquer ligação com a Tecnotrends.
