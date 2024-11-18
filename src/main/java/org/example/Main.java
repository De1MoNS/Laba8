import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
@Retention(RetentionPolicy.RUNTIME)
@interface Repeat {
    int value(); // Количество повторений
}

public class Main {
    public static void main(String[] args) {
        MyClass myClass = new MyClass();
        try {
            myClass.invokeAnnotatedMethods();
        } catch (Exception e) {
            e.printStackTrace();
        }
        String surname = "Vertikov";
        String name = "Yaroslav.txt";

        try {
            // a) Создайте директорию <surname>
            Path dir = Paths.get(surname);
            Files.createDirectories(dir);

            // b) Внутри директории <surname> создайте файл <name>
            Path file = dir.resolve(name);
            Files.createFile(file);

            // c) Создайте вложенные директории dir1, dir2, dir3 и скопируйте туда файл <name>
            for (String subDir : new String[]{"dir1", "dir2", "dir3"}) {
                Path subPath = dir.resolve(subDir);
                Files.createDirectory(subPath);
                Files.copy(file, subPath.resolve(name), StandardCopyOption.REPLACE_EXISTING);
            }

            // d) Внутри dir1 создайте файл file1
            Files.createFile(dir.resolve("dir1").resolve("file1.txt"));

            // e) Внутри dir2 создайте файл file2
            Files.createFile(dir.resolve("dir2").resolve("file2.txt"));

            // f) Рекурсивный обход директории <surname>
            System.out.println("Содержимое директории " + surname + ":");
            Files.walk(dir).forEach(path -> {
                String prefix = Files.isDirectory(path) ? "D: " : "F: ";
                System.out.println(prefix + path.getFileName());
            });

            // g) Удаление директории dir1 со всем её содержимым
            Path dir1 = dir.resolve("dir1");
            Files.walkFileTree(dir1, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
            System.out.println("Директория " + dir1.getFileName() + " удалена со всем её содержимым.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

    class MyClass {

    public void publicMethod() {
        System.out.println("Публичный метод");
    }

    protected void protectedMethod() {
        System.out.println("Защищенный метод");
    }
    @Repeat(3)
    protected void annotatedProtectedMethod() {
        System.out.println("Аннотированный защищенный метод");
    }
    @Repeat(2)
    private void annotatedPrivateMethod() {
        System.out.println("Аннотированный приватный метод");
    }

    // Метод для вызова всех аннотированных методов
    public void invokeAnnotatedMethods() throws Exception {
        for (var method : getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Repeat.class)) {
                int times = method.getAnnotation(Repeat.class).value();
                method.setAccessible(true); // Делаем приватные методы доступными
                for (int i = 0; i < times; i++) {
                    method.invoke(this);
                }
            }
        }
    }
}

